package cloud.carvis.api.util.mocks

import cloud.carvis.api.config.SecurityConfig
import cloud.carvis.api.user.model.UserDto
import cloud.carvis.api.util.helpers.MockServerUtils
import com.auth0.client.mgmt.ManagementAPI
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.verify.VerificationTimes
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder


@TestConfiguration
class Auth0Mock {

    companion object {
        private var mockServer = MockServerUtils.createMockServer()
    }

    @Bean
    fun managementApi(): ManagementAPI =
        ManagementAPI(this.getUrl(), "dummy")

    @Bean
    fun jwtDecoder(@Value("\${auth.audience}") audience: String): JwtDecoder {
        this.mockOidcConfigEndpoint()
        return SecurityConfig().jwtDecoder(audience, this.getUrl())
    }

    fun withUser(
        userId: String,
        name: String = "John Smith",
        email: String = "j+smith@example.com",
        company: String? = null,
        phone: String? = null
    ): Auth0Mock {
        val userMetadata = listOf(
            company?.let { "\"company\": \"$company\"" },
            phone?.let { "\"phone\": \"$phone\"" }
        )
        this.mockApiCall(
            path = "/api/v2/users/$userId",
            body = """
                {
                    "user_id": "$userId",
                    "name": "$name",
                    "email": "$email",
                    "user_metadata": {
                        ${userMetadata.filter { it?.isNotBlank() ?: false }.joinToString(",\n")}
                    }   
                }
                """
        )
        return this
    }

    fun withRole(roleName: String, roleId: String = "rol_1283injasd"): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/roles",
            body = """
                [
                  {
                    "id": "$roleId",
                    "name": "$roleName",
                    "description": "$roleName"
                  }
                ]
                """
        )
        return this
    }

    fun withUserRoleAssignment(roleId: String, email: String): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/roles/$roleId/users",
            body = """
                [
                  {
                    "user_id": "auth0|ijasdjiasdai",
                    "email": "dummy@dummy.com",
                    "picture": "http://foo.bar",
                    "name": "Foo Bar"
                  }
                ]
                """
        )
        return this
    }

    fun withUpdateResponse(user: UserDto): Auth0Mock {
        val userMetadata = listOf(
            user.company?.let { "\"company\": \"$it\"" },
            user.phone?.let { "\"phone\": \"$it\"" }
        )
        this.mockApiCall(
            path = "/api/v2/users/${user.userId}",
            method = "PATCH",
            body = """
                {
                    "user_id": "${user.userId}",
                    "name": "${user.name}",
                    "email": "${user.email}",
                    "user_metadata": {
                        ${userMetadata.filter { it?.isNotBlank() ?: false }.joinToString(",\n")}
                    }
                }
                """
        )
        return this
    }

    fun verify(request: HttpRequest, times: VerificationTimes) {
        mockServer.verify(request, times)
    }

    fun reset() =
        mockServer.reset()

    private fun mockOidcConfigEndpoint() =
        this.mockApiCall(
            "/.well-known/openid-configuration",
            MockServerUtils.createOidcConfigJson(this.getUrl())
        )

    private fun getUrl(): String =
        "http://localhost:${mockServer.port}/"

    private fun mockApiCall(
        path: String,
        body: String = "",
        method: String = "GET",
        statusCode: Int = 200,
        contentType: MediaType = APPLICATION_JSON
    ): Auth0Mock {
        mockServer.`when`(
            request()
                .withMethod(method)
                .withPath(path)
        )
            .respond(
                response()
                    .withStatusCode(statusCode)
                    .withContentType(contentType)
                    .withBody(body.trimIndent())
            )
        return this
    }
}
