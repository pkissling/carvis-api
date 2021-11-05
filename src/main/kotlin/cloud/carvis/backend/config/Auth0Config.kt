package cloud.carvis.backend.config

import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.net.AuthRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Auth0Config {

    @Bean
    fun managementApi(
        @Value("\${auth.domain}") domain: String,
        @Value("\${auth.client-id}") clientId: String,
        @Value("\${auth.client-secret}") clientSecret: String,
    ): ManagementAPI {
        val authAPI = AuthAPI(domain, clientId, clientSecret)
        val authRequest: AuthRequest = authAPI.requestToken(domain + "api/v2/")
        val holder = authRequest.execute()
        return ManagementAPI(domain, holder.accessToken)
    }
}
