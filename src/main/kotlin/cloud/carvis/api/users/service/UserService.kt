package cloud.carvis.api.users.service

import cloud.carvis.api.common.auth.service.AuthorizationService
import cloud.carvis.api.common.clients.Auth0RestClient
import cloud.carvis.api.common.events.model.UserSignupEvent
import cloud.carvis.api.users.dao.NewUserRepository
import cloud.carvis.api.users.mapper.UserMapper
import cloud.carvis.api.users.model.NewUserEntity
import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class UserService(
    private val auth0RestClient: Auth0RestClient,
    private val authorizationService: AuthorizationService,
    private val userMapper: UserMapper,
    private val newUserRepository: NewUserRepository,
) {

    private val logger = KotlinLogging.logger {}

    private val currentlyActiveUsers = ConcurrentHashMap<String, Instant>()

    fun updateUser(userId: String, user: UserDto): UserDto =
        userMapper.toEntity(user)
            .let { auth0RestClient.updateUser(userId, it) }
            .let { userMapper.toDto(it) }

    fun fetchUser(id: String): UserDto {
        return auth0RestClient.fetchUser(id)
            .let { userMapper.toDto(it) }
    }

    fun fetchOwnUser(): UserDto {
        val userId = authorizationService.getUserId()
        return fetchUser(userId)
    }

    fun fetchAllUsers(): List<UserDto> = auth0RestClient.fetchAllUsersWithRoles()
        .map { userMapper.toDto(it) }

    fun fetchUserSafe(userId: String): UserDto? = try {
        this.fetchUser(userId)
    } catch (e: Exception) {
        logger.warn { "Error while fetching user with userId: $userId" }
        null
    }

    fun addUserRoles(userId: String, addRoles: List<UserRole>) {
        if (addRoles.isEmpty()) {
            logger.info { "No role to be added provided for userId: $userId" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "no roles to add provided")
        }
        auth0RestClient.addUserRole(userId, addRoles)
        val hasNewUserEntity = newUserRepository.existsByHashKey(userId)
        if (hasNewUserEntity) {
            logger.info { "Deleting new user entity for userId: $userId" }
            newUserRepository.deleteByHashKey(userId)
        }
    }

    fun removeUserRoles(userId: String, removeRoles: List<UserRole>) {
        if (removeRoles.isEmpty()) {
            logger.info { "No role to be removed provided for userId: $userId" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "no roles to remove provided")
        }
        auth0RestClient.removeUserRole(userId, removeRoles)
    }

    fun deleteUser(userId: String) {
        auth0RestClient.deleteUser(userId)
        if (newUserRepository.existsByHashKey(userId)) {
            newUserRepository.deleteByHashKey(userId)
        }
    }

    fun newUsersCount(): Int =
        newUserRepository.count()

    fun persistNewUserSignup(event: UserSignupEvent) {
        val exists = newUserRepository.existsByHashKey(event.userId)
        if (exists) {
            logger.warn { "New user already persisted with userId [${event.userId}]. Skipping..." }
            return
        }
        newUserRepository.save(NewUserEntity(userId = event.userId))
    }

    fun usersCount() = auth0RestClient.fetchAllUsers().count()

    fun monthlyActiveUsersCount(): Int = auth0RestClient.monthlyActiveUsersCount()

    fun dailyLoginsCount(): Int = auth0RestClient.dailyLoginsCount()
    fun fetchCurrentlyActiveUsersCount(): Int = currentlyActiveUsers
        .filterValues { it.isAfter(Instant.now().minusSeconds(300)) }
        .size

    fun addCurrentlyActiveUser(userName: String): Boolean {
        val newUser = !currentlyActiveUsers.contains(userName)
        currentlyActiveUsers[userName] = Instant.now()
        return newUser
    }

    fun updateOwnUser(user: UserDto): UserDto {
        val userId = this.authorizationService.getUserId()
        return this.updateUser(userId, user)
    }
}
