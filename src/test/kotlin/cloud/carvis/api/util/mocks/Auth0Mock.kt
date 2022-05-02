package cloud.carvis.api.util.mocks

import cloud.carvis.api.config.SecurityConfig
import cloud.carvis.api.user.model.UserDto
import cloud.carvis.api.user.model.UserRole
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

    fun withUser(user: UserDto): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/users/${user.userId}",
            body = userJson(user)
        )
        this.mockApiCall(
            path = "/api/v2/users/${user.userId}/roles",
            body = rolesJson(*user.roles.toTypedArray())
        )
        return this
    }

    fun withRoleUsers(vararg users: UserDto): Auth0Mock {
        val roles: Map<UserRole, List<UserDto>> = users
            .flatMap { it.roles }
            .distinct()
            .associateWith { role -> users.filter { it.roles.contains(role) } }
        roles.forEach { (roleName, users) ->
            this.mockApiCall(
                path = "/api/v2/roles/id_$roleName/users",
                body = usersJson(*users.toTypedArray())
            )
        }
        return this
    }

    fun withUpdateResponse(user: UserDto): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/users/${user.userId}",
            method = "PATCH",
            body = userJson(user)
        )
        return this
    }

    fun withUsers(vararg users: UserDto): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/users",
            body = usersJson(*users)
        )
        withRoles(*users)
        withRoleUsers(*users)
        return this
    }

    fun withRoles(vararg users: UserDto) {
        val distinctRoles = users
            .flatMap { it.roles }
            .distinct()
            .toTypedArray()
        this.mockApiCall(
            path = "/api/v2/roles",
            body = rolesJson(*distinctRoles)
        )
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

    private fun userJson(user: UserDto): String {
        val userMetadata = listOf(
            user.company?.let { "\"company\": \"$it\"" },
            user.phone?.let { "\"phone\": \"$it\"" }
        )
        return """
            {
                "user_id": "${user.userId}",
                "name": "${user.name}",
                "email": "${user.email}",
                "user_metadata": {
                    ${userMetadata.filter { it?.isNotBlank() ?: false }.joinToString(",\n")}
                }
            }
            """
    }

    private fun usersJson(vararg users: UserDto): String {
        return """
            [
                ${users.joinToString(",\n") { userJson(it) }}
            ]
            """
    }

    private fun roleJson(roleName: UserRole): String {
        return """
            {
                "id": "id_${roleName.toJsonValue()}",
                "name": "${roleName.toJsonValue()}",
                "description": "description_${roleName.toJsonValue()}"
            }
            """
    }

    private fun rolesJson(vararg roleNames: UserRole): String {
        return """
            [
                ${roleNames.joinToString(",\n") { roleJson(it) }}
            ]
            """
    }
}
