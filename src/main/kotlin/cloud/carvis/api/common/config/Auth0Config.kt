package cloud.carvis.api.common.config

import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule


@Configuration
class Auth0Config {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun authApi(
        @Value("\${auth.client-id}") clientId: String,
        @Value("\${auth.client-secret}") clientSecret: String,
        @Value("\${auth.domain}") domain: String
    ): AuthAPI = AuthAPI(domain, clientId, clientSecret)

    @Bean
    fun managementApi(
        @Value("\${auth.client-id}") clientId: String,
        @Value("\${auth.client-secret}") clientSecret: String,
        @Value("\${auth.domain}") domain: String,
        authAPI: AuthAPI
    ): ManagementAPI {
        val (apiToken, expiresIn) = fetchAccessToken(authAPI, domain)
        return ManagementAPI(domain, apiToken)
            .also { scheduleTokenRenewal(expiresIn, authAPI, it, domain) }
    }

    fun scheduleTokenRenewal(
        expiresIn: Duration,
        authApi: AuthAPI,
        managementApi: ManagementAPI,
        domain: String,
        isRetry: Boolean = false
    ) {
        logger.info { "Scheduling next renewal of Auth0 token at: ${Instant.now().plus(expiresIn)}" }
        Timer("Auth0 token refresh", true).schedule(expiresIn.toMillis()) {
            try {
                val (apiToken, newTokenExpiresIn) = fetchAccessToken(authApi, domain)
                managementApi.setApiToken(apiToken)
                logger.error("ErrRenewed Auth0 token.") // TODO
                logger.warn("WarnRenewed Auth0 token.") // TODO
                scheduleTokenRenewal(newTokenExpiresIn, authApi, managementApi, domain)
            } catch (e: Exception) {
                val retry = if (isRetry) {
                    min(expiresIn.multipliedBy(2), Duration.ofMinutes(1))
                } else {
                    Duration.ofSeconds(1)
                }
                logger.error(e) { "Unable to renew Auth0 token. Rescheduling in ${retry}." }
                scheduleTokenRenewal(retry, authApi, managementApi, domain, true)
            }
        }
    }

    private fun fetchAccessToken(authAPI: AuthAPI, domain: String): Pair<String, Duration> = try {
        val authHolder = authAPI
            .requestToken(domain + "api/v2/")
            .execute()
        authHolder.accessToken to Duration.ofSeconds(authHolder.expiresIn)
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch Auth0 accessToken" }
        throw e
    }

    private fun min(d1: Duration, d2: Duration) =
        if (d1 > d2) {
            d2
        } else {
            d1
        }
}
