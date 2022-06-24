package cloud.carvis.api.util.mocks

import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.util.helpers.MockServerUtils
import org.mockserver.matchers.Times
import org.mockserver.matchers.Times.exactly
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.verify.VerificationTimes
import org.mockserver.verify.VerificationTimes.once
import org.springframework.boot.test.context.TestConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@TestConfiguration
class Auth0Mock {

    companion object {
        private var mockServer = MockServerUtils.createMockServer()

        fun getMockUrl(): String =
            "http://localhost:${mockServer.port}/"

        fun withApiToken() = mockApiCall(
            path = "/oauth/token",
            method = "POST",
            body = """
                  { 
                    "access_token": "foobar",
                    "expires_in": 9999
                  }
                  """
        )

        fun withOidcEndpoint() = mockApiCall(
            path = "/.well-known/openid-configuration",
            body = MockServerUtils.createOidcConfigJson(getMockUrl())
        )

        private fun mockApiCall(
            path: String,
            body: String = "",
            method: String = "GET",
            statusCode: Int = 200,
            contentType: MediaType = APPLICATION_JSON,
            queryParams: Map<String, String> = emptyMap(),
            times: Times = Times.unlimited()
        ) {
            mockServer.`when`(
                request()
                    .withMethod(method)
                    .withPath(path)
                    .apply {
                        queryParams.forEach { withQueryStringParameter(it.key, it.value) }
                    },
                times
            )
                .respond(
                    response()
                        .withStatusCode(statusCode)
                        .withContentType(contentType)
                        .withBody(body.trimIndent())
                )
        }
    }


    fun withRoleUsers(vararg users: UserDto): Auth0Mock {
        val roles: Map<UserRole, List<UserDto>> = users
            .flatMap { it.roles }
            .distinct()
            .associateWith { role -> users.filter { it.roles.contains(role) } }
        roles.forEach { (roleName, users) ->
            mockApiCall(
                path = "/api/v2/roles/id_$roleName/users",
                body = usersJson(*users.toTypedArray())
            )
        }
        return this
    }

    fun withUpdateResponse(user: UserDto): Auth0Mock {
        mockApiCall(
            path = "/api/v2/users/${user.userId}",
            method = "PATCH",
            body = userJson(user)
        )
        return this
    }

    fun withUsers(vararg users: UserDto): Auth0Mock {
        mockApiCall(
            path = "/api/v2/users",
            body = usersJson(*users)
        )
        users.forEach { withUser(it) }
        withRoles(*users)
        withRoleUsers(*users)
        return this
    }

    fun withUser(user: UserDto): Auth0Mock {
        mockApiCall(
            path = "/api/v2/users/${user.userId}",
            body = userJson(user)
        )
        return this
    }

    fun withRoles(vararg users: UserDto): Auth0Mock {
        val distinctRoles = users
            .flatMap { it.roles }
            .distinct()
            .toTypedArray()
        mockApiCall(
            path = "/api/v2/roles",
            body = rolesJson(*distinctRoles)
        )
        distinctRoles.forEach { withRole(it) }
        users.forEach { withUserRoles(it) }
        return this
    }

    fun withUserRoles(user: UserDto) {
        mockApiCall(
            path = "/api/v2/users/${user.userId}/roles",
            body = rolesJson(*user.roles.toTypedArray()),
        )
    }

    fun withRole(role: UserRole): Auth0Mock {
        mockApiCall(
            path = "/api/v2/roles",
            body = rolesJson(role),
            queryParams = mapOf("name_filter" to role.toJsonValue())
        )
        mockApiCall(
            path = "/api/v2/roles/id_$role",
            body = roleJson(role)
        )
        return this
    }

    fun withAddRoleResponse(userId: String) = mockApiCall(
        path = "/api/v2/users/$userId/roles",
        method = "POST",
        statusCode = 200
    )

    fun withRemoveRoleResponse(userId: String) = mockApiCall(
        path = "/api/v2/users/$userId/roles",
        method = "DELETE",
        statusCode = 200
    )

    fun withDeleteUserResponse(userId: String) = mockApiCall(
        path = "/api/v2/users/$userId",
        method = "DELETE",
        statusCode = 200
    )

    fun withActiveUsers(count: Int) = mockApiCall(
        path = "/api/v2/stats/active-users",
        body = "$count",
        statusCode = 200
    )

    fun withDailyLogins(logins: Int?) = mockApiCall(
        path = "/api/v2/stats/daily",
        body = dailyStatsJson(logins),
        statusCode = 200
    )

    fun withApiTokenError(times: Int): Auth0Mock {
        mockApiCall(
            path = "/oauth/token",
            method = "POST",
            statusCode = 500,
            times = exactly(times)
        )
        return this
    }

    fun withApiToken(): Auth0Mock {
        Auth0Mock.withApiToken()
        return this
    }

    fun withOidcEndpoint(): Auth0Mock {
        Auth0Mock.withOidcEndpoint()
        return this
    }

    fun withRolesError(queryParams: Map<String, String>): Auth0Mock {
        mockApiCall(
            path = "/api/v2/roles",
            queryParams = queryParams,
            statusCode = 500
        )
        return this
    }

    fun verify(request: HttpRequest, times: VerificationTimes = once()): Auth0Mock {
        mockServer.verify(request, times)
        return this
    }

    fun reset(): Auth0Mock {
        mockServer.reset()
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
                },
                "picture": "${user.picture}"
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

    private fun dailyStatsJson(logins: Int?): String {
        if (logins == null) {
            return "[]"
        }
        val date = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withZone(ZoneId.from(ZoneOffset.UTC))
            .format(Instant.now())
        return """
           [
              {
                "date": "$date",
                "logins": $logins,
                "signups": 100,
                "leaked_passwords": 100,
                "updated_at": "2014-01-01T02:00:00.000Z",
                "created_at": "2014-01-01T20:00:00.000Z"
              }
            ]
            """
    }
}
