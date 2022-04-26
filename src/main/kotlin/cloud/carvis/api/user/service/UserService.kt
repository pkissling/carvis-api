package cloud.carvis.api.user.service

import cloud.carvis.api.clients.Auth0RestClient
import cloud.carvis.api.service.AuthorizationService
import cloud.carvis.api.user.mapper.UserMapper
import cloud.carvis.api.user.model.UserDto
import mu.KotlinLogging
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class UserService(
    private val auth0RestClient: Auth0RestClient,
    private val authorizationService: AuthorizationService,
    private val userMapper: UserMapper
) {

    private val logger = KotlinLogging.logger {}

    fun fetchAllAdminEmails(): List<String> {
        val admins = auth0RestClient.fetchAllAdmins()
        if (admins.isEmpty()) {
            throw RuntimeException("No users with admin role returned by Auth0.")
        }

        val adminsWithoutEmail = admins
            .filter { it.email.isNullOrBlank() }

        if (adminsWithoutEmail.isNotEmpty()) {
            logger.warn { "Following admins have no email address set in Auth0: $adminsWithoutEmail" }
        }

        return admins
            .map { it.email }
            .also { logger.debug { "Resolved following admin emails: $it" } }
    }

    @PreAuthorize("@authorization.canAccessAndModifyUser(#id)")
    fun updateUser(id: String, user: UserDto): UserDto =
        userMapper.toEntity(user)
            .let { auth0RestClient.updateUser(id, it) }
            .let { userMapper.toDto(it) }

    fun fetchUser(id: String): UserDto {
        return auth0RestClient.fetchUser(id)
            .let { userMapper.toDto(it) }
    }

    fun fetchOwnUser(): UserDto {
        val userId = authorizationService.getUserId()
        return fetchUser(userId)
    }

    @PreAuthorize("@authorization.isAdmin()")
    fun fetchAllUsers(): List<UserDto> {
        return auth0RestClient.fetchAllUsers()
            .map { userMapper.toDto(it) }
    }

    fun fetchUserSafe(userId: String): UserDto? = try {
        this.fetchUser(userId)
    } catch (e: Exception) {
        logger.warn { "Error while fetching user with userId: $userId" }
        null
    }
}
