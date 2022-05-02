package cloud.carvis.api.clients

import cloud.carvis.api.user.model.UserRole
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
    fun fetchUser(userId: String): UserWithRoles {
        val user = withErrorHandling {
            managementApi.users()
                .get(userId, null)
                .execute()
        }

        val roles = withErrorHandling {
            managementApi.users()
                .listRoles(userId, null)
                .execute()
                .items
                .map { UserRole.from(it.name) }
        }

        return UserWithRoles(user, roles)
    }

    fun fetchAllAdmins(): List<User> = try {
        val adminRole = managementApi.roles()
            .list(rolesFilter("admin"))
            .execute()
            .items
            .firstOrNull()
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
    fun updateUser(userId: String, userWithRoles: UserWithRoles): UserWithRoles {
        val updatedUser = withErrorHandling {
            managementApi.users()
                .update(userId, userWithRoles.user)
                .execute()
        }

        val roles = withErrorHandling {
            managementApi.users()
                .listRoles(userId, null)
                .execute()
                .items
                .map { UserRole.from(it.name) }
        }

        return UserWithRoles(updatedUser, roles)
    }

    @Cacheable("auth0-users", sync = true)
    fun fetchAllUsers(): List<UserWithRoles> {
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

        val usersWithRole = roles.flatMap { role ->
            withErrorHandling {
                managementApi.roles()
                    .listUsers(role.id, null)
                    .execute()
                    .items
            }
                .associate { user -> Pair(user.id, UserRole.from(role.name)) }
                .asSequence()

        }
            .groupBy({ it.key }, { it.value })
            .map { (userId, roles) -> UserWithRoles(users.first { it.id == userId }, roles) }

        val usersWithoutRole = users
            .filter { user -> !usersWithRole.map { it.user.id }.contains(user.id) }
            .map { UserWithRoles(it, emptyList()) }

        return usersWithRole + usersWithoutRole
    }

    fun addUserRole(userId: String, addRoles: List<String>) {
        val roleIds = withErrorHandling {
            managementApi.roles()
                .list(rolesFilter(*addRoles.toTypedArray()))
                .execute()
                .items
                .map { it.id }
        }
        withErrorHandling {
            managementApi.users()
                .addRoles(userId, roleIds)
                .execute()
        }
    }

    fun removeUserRole(userId: String, removeRoles: List<String>) {
        val roleIds = withErrorHandling {
            managementApi.roles()
                .list(rolesFilter(*removeRoles.toTypedArray()))
                .execute()
                .items
                .map { it.id }
        }
        withErrorHandling {
            managementApi.users()
                .removeRoles(userId, roleIds)
                .execute()
        }
    }


    private fun rolesFilter(vararg addRoles: String): RolesFilter =
        RolesFilter().also { roleFilter -> addRoles.forEach { role -> roleFilter.withName(role) } }

    private fun <T : Any> withErrorHandling(fn: () -> T): T = try {
        fn.invoke()
    } catch (e: APIException) {
        logger.error(e) { "Error while calling Auth0" }
        val statusCode = if (e.statusCode == 404) HttpStatus.NOT_FOUND else HttpStatus.BAD_REQUEST
        throw ResponseStatusException(statusCode, "Auth0 message: ${e.message}")
    } catch (e: Exception) {
        logger.error(e) { "Error while calling Auth0" }
        throw e
    }
}

data class UserWithRoles(val user: User, val roles: List<UserRole>)
