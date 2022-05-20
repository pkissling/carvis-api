package cloud.carvis.api.users.rest

import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping
class UserRestController(
    val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/my-user")
    fun fetchOwnUser(): UserDto {
        logger.info { "start fetchOwnUser()" }
        return userService.fetchOwnUser()
            .also { logger.info { "end fetchOwnUser() return=${it}" } }
    }

    @GetMapping("/users/{userId}")
    fun fetchUser(@PathVariable userId: String): UserDto {
        logger.info { "start fetchUser(userId=$userId)" }
        return userService.fetchUser(userId)
            .also { logger.info { "end fetchUser(userId=$userId) return=${it}" } }
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("@authorization.canAccessAndModifyUser(#userId)")
    fun updateUser(@PathVariable userId: String, @Valid @RequestBody user: UserDto): UserDto {
        logger.info { "start updateUser(userId=$userId,user=$user)" }
        return userService.updateUser(userId, user)
            .also { logger.info { "end updateUser(userId=$userId,user=$user), return=${it}" } }
    }

    @GetMapping("/users")
    @PreAuthorize("@authorization.isAdmin()")
    fun fetchAllUsers(): List<UserDto> {
        logger.info { "start fetchAllUsers()" }
        return userService.fetchAllUsers()
            .also { logger.info { "end fetchAllUsers() return=${it}" } }
    }

    @PostMapping("/users/{userId}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authorization.isAdmin()")
    fun addUserRoles(@PathVariable userId: String, @RequestBody addRoles: List<UserRole>) {
        logger.info { "start addUserRoles(userId=$userId,addRoles=$addRoles)" }
        return userService.addUserRoles(userId, addRoles)
            .also { logger.info { "end addUserRoles(userId=$userId,addRoles=$addRoles)" } }
    }

    @DeleteMapping("/users/{userId}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authorization.isAdmin()")
    fun removeUserRoles(@PathVariable userId: String, @RequestBody removeRoles: List<UserRole>) {
        logger.info { "start removeUserRoles(userId=$userId,removeRoles=$removeRoles)" }
        userService.removeUserRoles(userId, removeRoles)
            .also { logger.info { "end removeUserRoles(userId=$userId,removeRoles=$removeRoles)" } }
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("@authorization.isAdmin()")
    fun deleteUser(@PathVariable userId: String) {
        logger.info { "start deleteUser(userId=$userId)" }
        userService.deleteUser(userId)
            .also { logger.info { "end deleteUser(userId=$userId)" } }
    }

    @GetMapping("/new-users-count")
    @PreAuthorize("@authorization.isAdmin()")
    fun fetchNewUsersCount(): Long {
        logger.info { "start fetchNewUsersCount()" }
        return userService.newUsersCount()
            .also { logger.info { "end fetchNewUsersCount() return=${it}" } }

    }
}
