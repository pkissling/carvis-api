package cloud.carvis.api.clients

import cloud.carvis.api.user.model.UserRole
import cloud.carvis.api.user.model.UserRole.ADMIN
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.RolesFilter
import com.auth0.exception.APIException
import com.auth0.json.mgmt.users.User
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class Auth0RestClient(private val managementApi: ManagementAPI) {

    private val logger = KotlinLogging.logger {}

    @Cacheable("auth0-users", key = "#userId", sync = true)
    fun fetchUser(userId: String): UserWithRoles {
        val user = withErrorHandling(NOT_FOUND) {
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
            .list(rolesFilter(ADMIN))
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

    @CachePut("auth0-users", key = "#userId")
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

    @CacheEvict("auth0-users", key = "#userId")
    fun addUserRole(userId: String, addRoles: List<UserRole>) {
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

    @CacheEvict("auth0-users", key = "#userId")
    fun removeUserRole(userId: String, removeRoles: List<UserRole>) {
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

    @CacheEvict("auth0-users", key = "#userId")
    fun deleteUser(userId: String) {
        withErrorHandling(NOT_FOUND) {
            managementApi.users()
                .delete(userId)
                .execute()
        }
    }

    private fun rolesFilter(vararg roles: UserRole): RolesFilter =
        RolesFilter().also { roleFilter ->
            roles
                .map { it.toJsonValue() }
                .forEach { role -> roleFilter.withName(role) }
        }

    private fun <T : Any> withErrorHandling(except: HttpStatus? = null, fn: () -> T): T = try {
        fn.invoke()
    } catch (e: Exception) {
        logger.error(e) { "Error while calling Auth0" }
        if (e is APIException && e.statusCode == except?.value()) {
            throw ResponseStatusException(e.statusCode, "Auth0 message: ${e.message}", e)
        }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "Auth0 message: ${e.message}", e)
    }
}

data class UserWithRoles(val user: User, val roles: List<UserRole>)
