package cloud.carvis.api.clients

import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.RolesFilter
import com.auth0.exception.APIException
import com.auth0.json.mgmt.users.User
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class Auth0RestClient(private val managementApi: ManagementAPI) {

    private val logger = KotlinLogging.logger {}

    @Cacheable("auth0-user", sync = true)
    fun fetchUser(userId: String): User =
        withErrorHandling {
            managementApi.users()
                .get(userId, null)
                .execute()
        }

    fun fetchAllAdmins(): List<User> = try {
        val roles = managementApi.roles()
            .list(RolesFilter().withName("admin"))
            .execute()
            .items

        if (roles.size > 1) {
            throw RuntimeException("Auth0 did return more than 1 role for roleName 'admin': $roles")
        }

        val adminRole = roles.firstOrNull()
            ?: throw RuntimeException("Auth0 did return not return an roleId from role 'admin'")

        managementApi.roles()
            .listUsers(adminRole.id, null)
            .execute()
            .items
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch all admins from Auth0" }
        emptyList()
    }

    @CacheEvict("auth0-users", key = "#userId")
    fun updateUser(userId: String, user: User): User =
        withErrorHandling {
            managementApi.users()
                .update(userId, user)
                .execute()
        }

    @Cacheable("auth0-users", sync = true)
    fun fetchAllUsers(): List<Pair<User, List<String>>> {
        val users = withErrorHandling {
            managementApi.users()
                .list(null)
                .execute()
                .items
        }

        val roles = withErrorHandling {
            managementApi.roles()
                .list(null)
                .execute()
                .items
        }

        val userRoleMap = roles.flatMap { role ->
            withErrorHandling {
                managementApi.roles()
                    .listUsers(role.id, null)
                    .execute()
                    .items
            }
                .associate { user -> Pair(user.id, role.name) }
                .asSequence()

        }
            .distinct()
            .groupBy({ it.key }, { it.value })

        return users
            .map { Pair(it, userRoleMap[it.id] ?: emptyList()) }
            .toList()
    }


    private fun <T : Any> withErrorHandling(fn: () -> T): T = try {
        fn.invoke()
    } catch (e: APIException) {
        logger.error(e) { "Error while calling Auth0" }
        throw ResponseStatusException(HttpStatus.valueOf(e.statusCode), e.message)
    } catch (e: Exception) {
        logger.error(e) { "Error while calling Auth0" }
        throw e
    }
}
