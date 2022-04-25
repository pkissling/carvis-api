package cloud.carvis.api.user.service

import cloud.carvis.api.clients.Auth0RestClient
import cloud.carvis.api.service.AuthorizationService
import cloud.carvis.api.user.mapper.UserMapper
import cloud.carvis.api.user.model.UserDto
import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val auth0RestClient: Auth0RestClient,
    private val authorizationService: AuthorizationService,
    private val userMapper: UserMapper
) {

    private val logger = KotlinLogging.logger {}

    @Cacheable("userNames", sync = true)
    fun fetchName(userId: String): String? =
        auth0RestClient.fetchUser(userId)
            ?.name
            .also { logger.debug { "Resolved name [$it] for userId: $userId" } }

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
    fun updateUser(id: String, user: UserDto): UserDto {
        if (auth0RestClient.fetchUser(id) == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with id [$id] not found.")
        }
        val updatedUser = userMapper.toEntity(user)
            .let { auth0RestClient.updateUser(id, it) }

        if (updatedUser == null) {
            logger.warn { "Error while updating user with id [$id]" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot update user")
        }

        return userMapper.toDto(updatedUser)
    }

    @PreAuthorize("@authorization.canAccessAndModifyUser(#id)")
    fun fetchUser(id: String): UserDto {
        return auth0RestClient.fetchUser(id)
            ?.let { userMapper.toDto(it) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
    }

    fun fetchOwnUser(): UserDto {
        val userId = authorizationService.getUserId()
        return auth0RestClient.fetchUser(userId)
            ?.let { userMapper.toDto(it) }
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to extract user from auth context")
    }
}
