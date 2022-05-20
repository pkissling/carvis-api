package cloud.carvis.api.common.config

import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.net.AuthRequest
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*
import kotlin.concurrent.schedule


@Configuration
class Auth0Config {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun managementApi(
        @Value("\${auth.domain}") domain: String,
        @Value("\${auth.client-id}") clientId: String,
        @Value("\${auth.client-secret}") clientSecret: String,
    ): ManagementAPI {
        val authAPI = AuthAPI(domain, clientId, clientSecret)
        val token = fetchAccessToken(authAPI, domain)
        return ManagementAPI(domain, token.apiToken)
            .also { scheduleTokenRenewal(token.expiresIn, it) { fetchAccessToken(authAPI, domain) } }
    }

    private fun scheduleTokenRenewal(expiresIn: Duration, managementApi: ManagementAPI, supplier: () -> Token) {
        logger.info { "Scheduling next renewal of Auth0 token at: ${Instant.now().plus(expiresIn)}" }
        val delayInMs = expiresIn.minus(1, MINUTES).toMillis()
        Timer("Auth0 token refresh", true).schedule(delayInMs) {
            val token = supplier.invoke()
            managementApi.setApiToken(token.apiToken)
            logger.info("Renewed Auth0 token.")
            scheduleTokenRenewal(token.expiresIn, managementApi, supplier)
        }
    }

    private fun fetchAccessToken(authAPI: AuthAPI, domain: String): Token = try {
        val authRequest: AuthRequest = authAPI.requestToken(domain + "api/v2/")
        val holder = authRequest.execute()
        Token(holder.accessToken, Duration.ofSeconds(holder.expiresIn))
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch Auth0 accessToken" }
        throw e
    }

    data class Token(val apiToken: String, val expiresIn: Duration)
}
