package cloud.carvis.api.mocks

import cloud.carvis.api.config.SecurityConfig
import com.auth0.client.mgmt.ManagementAPI
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.net.ServerSocket


@TestConfiguration
class Auth0Mock {

    @Autowired
    private lateinit var auth0MockServer: Auth0MockServer

    fun withUser(
        userId: String,
        username: String = "j+smith",
        name: String = "John Smith",
        email: String = "j+smith@example.com"
    ): Auth0Mock {
        this.auth0MockServer.mockApiCall(
            "/api/v2/users/${userId}",
            """
            {
                "userId":"$userId",
                "username":"$username",
                "name":"$name",
                "email":"$email",
                "createdAt":"2021-12-07T18:10:59.126Z"
            }
            """.trimIndent()
        )
        return this
    }

    fun withRole(roleName: String, roleId: String = "rol_1283injasd") {
        this.auth0MockServer.mockApiCall(
            "/api/v2/roles", """
            [
              {
                "id": "$roleId",
                "name": "$roleName",
                "description": "$roleName"
              }
            ]
            """.trimIndent()
        )

    }

    fun withUserRoleAssignment(roleId: String, email: String) {
        this.auth0MockServer.mockApiCall(
            "/api/v2/roles/$roleId/users", """
            [
              {
                "user_id": "auth0|ijasdjiasdai",
                "email": "dummy@dummy.com",
                "picture": "http://foo.bar",
                "name": "Foo Bar"
              }
            ]
            """.trimIndent()
        )
    }

    fun reset() {
        this.auth0MockServer.reset()
    }

    @Configuration
    class SecurityConfiguration {
        @Bean
        fun managementApi(auth0MockServer: Auth0MockServer): ManagementAPI =
            ManagementAPI(auth0MockServer.getUrl(), "dummy")

        @Bean
        fun auth0MockServer(): Auth0MockServer {
            val unusedPort = findUnusedPort()
            val mockServerClient = startClientAndServer(unusedPort)
            return Auth0MockServer(mockServerClient)
        }

        @Bean
        fun jwtDecoder(
            @Value("\${auth.audience}") audience: String,
            auth0MockServer: Auth0MockServer
        ): JwtDecoder =
            SecurityConfig().jwtDecoder(audience, auth0MockServer.getUrl())

        private fun findUnusedPort(): Int {
            val socket = ServerSocket(0)
            return socket.localPort
                .also { socket.close() }
        }
    }

    class Auth0MockServer(
        private val mockServer: ClientAndServer
    ) {

        init {
            this.mockWellKnownEndpoint()
        }

        fun reset() {
            this.mockServer.reset()
        }

        private fun mockWellKnownEndpoint() {
            this.mockApiCall(
                "/.well-known/openid-configuration", createOpenIdConfiguration()
            )

        }

        private fun createOpenIdConfiguration() =
            """
        {
          "issuer": "${this.getUrl()}",
          "authorization_endpoint": "https://carvis.eu.auth0.com/authorize",
          "token_endpoint": "https://carvis.eu.auth0.com/oauth/token",
          "device_authorization_endpoint": "https://carvis.eu.auth0.com/oauth/device/code",
          "userinfo_endpoint": "https://carvis.eu.auth0.com/userinfo",
          "mfa_challenge_endpoint": "https://carvis.eu.auth0.com/mfa/challenge",
          "jwks_uri": "https://carvis.eu.auth0.com/.well-known/jwks.json",
          "registration_endpoint": "https://carvis.eu.auth0.com/oidc/register",
          "revocation_endpoint": "https://carvis.eu.auth0.com/oauth/revoke",
          "scopes_supported": [
            "openid",
            "profile",
            "offline_access",
            "name",
            "given_name",
            "family_name",
            "nickname",
            "email",
            "email_verified",
            "picture",
            "created_at",
            "identities",
            "phone",
            "address"
          ],
          "response_types_supported": [
            "code",
            "token",
            "id_token",
            "code token",
            "code id_token",
            "token id_token",
            "code token id_token"
          ],
          "code_challenge_methods_supported": [
            "S256",
            "plain"
          ],
          "response_modes_supported": [
            "query",
            "fragment",
            "form_post"
          ],
          "subject_types_supported": [
            "public"
          ],
          "id_token_signing_alg_values_supported": [
            "HS256",
            "RS256"
          ],
          "token_endpoint_auth_methods_supported": [
            "client_secret_basic",
            "client_secret_post"
          ],
          "claims_supported": [
            "aud",
            "auth_time",
            "created_at",
            "email",
            "email_verified",
            "exp",
            "family_name",
            "given_name",
            "iat",
            "identities",
            "iss",
            "name",
            "nickname",
            "phone_number",
            "picture",
            "sub"
          ],
          "request_uri_parameter_supported": false
        }
        """.trimIndent()

        fun getUrl(): String =
            "http://localhost:${mockServer.port}"

        fun mockApiCall(
            path: String,
            body: String,
            method: String = "GET",
            statusCode: Int = 200,
            contentType: MediaType = MediaType.APPLICATION_JSON
        ) {
            this.mockServer.`when`(
                HttpRequest.request()
                    .withMethod(method)
                    .withPath(path)
            )
                .respond(
                    HttpResponse.response()
                        .withStatusCode(statusCode)
                        .withContentType(contentType)
                        .withBody(body.trimIndent())
                )
        }
    }
}

