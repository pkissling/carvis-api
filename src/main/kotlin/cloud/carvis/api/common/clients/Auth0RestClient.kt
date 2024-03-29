package cloud.carvis.api.common.clients

import cloud.carvis.api.users.model.UserRole
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
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit.DAYS
import java.util.*

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
                .mapNotNull { UserRole.from(it.name) }
        }

        return UserWithRoles(user, roles)
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
                .mapNotNull { UserRole.from(it.name) }
        }

        return UserWithRoles(updatedUser, roles)
    }

    fun fetchAllUsersWithRoles(): List<UserWithRoles> {
        val users = fetchAllUsers()

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
                .associate { user -> Pair(user.id, UserRole.from(role.name) ?: throw RuntimeException("Unknown role: ${role.name}")) }
                .asSequence()
        }
            .groupBy({ it.key }, { it.value })
            .map { (userId, roles) -> UserWithRoles(users.first { it.id == userId }, roles) }

        val usersWithoutRole = users
            .filter { user -> !usersWithRole.map { it.user.id }.contains(user.id) }
            .map { UserWithRoles(it, emptyList()) }

        return usersWithRole + usersWithoutRole
    }

    fun fetchAllUsers(): List<User> = withErrorHandling {
        managementApi.users()
            .list(null)
            .execute()
            .items
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

    fun monthlyActiveUsersCount(): Int = withErrorHandling {
        managementApi.stats()
            .activeUsersCount
            .execute()
    }

    fun dailyLoginsCount(): Int = withErrorHandling {
        val today = Instant.now()
            .atZone(ZoneId.of("UTC"))
            .truncatedTo(DAYS)
            .toInstant()
            .let { Date.from(it) }

        managementApi.stats()
            .getDailyStats(today, today)
            .execute()
            .sortedByDescending { it.date }
            .firstOrNull()
            ?.logins
            ?: 0
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
