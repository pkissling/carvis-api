package cloud.carvis.api.users.rest

import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/admin/users")
class AdminUserRestController(
    val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{userId}")
    fun fetchUser(@PathVariable userId: String): UserDto {
        logger.info { "start fetchUser(userId=$userId)" }
        return userService.fetchUser(userId)
            .also { logger.info { "end fetchUser(userId=$userId) return=${it}" } }
    }

    @PutMapping("/{userId}")
    fun updateUser(@PathVariable userId: String, @Valid @RequestBody user: UserDto): UserDto {
        logger.info { "start updateUser(userId=$userId,user=$user)" }
        return userService.updateUser(userId, user)
            .also { logger.info { "end updateUser(userId=$userId,user=$user), return=${it}" } }
    }

    @GetMapping
    fun fetchAllUsers(): List<UserDto> {
        logger.info { "start fetchAllUsers()" }
        return userService.fetchAllUsers()
            .also { logger.info { "end fetchAllUsers() return=${it}" } }
    }

    @PostMapping("/{userId}/roles")
    @ResponseStatus(NO_CONTENT)
    fun addUserRoles(@PathVariable userId: String, @RequestBody addRoles: List<UserRole>) {
        logger.info { "start addUserRoles(userId=$userId,addRoles=$addRoles)" }
        return userService.addUserRoles(userId, addRoles)
            .also { logger.info { "end addUserRoles(userId=$userId,addRoles=$addRoles)" } }
    }

    @DeleteMapping("/{userId}/roles")
    @ResponseStatus(NO_CONTENT)
    fun removeUserRoles(@PathVariable userId: String, @RequestBody removeRoles: List<UserRole>) {
        logger.info { "start removeUserRoles(userId=$userId,removeRoles=$removeRoles)" }
        userService.removeUserRoles(userId, removeRoles)
            .also { logger.info { "end removeUserRoles(userId=$userId,removeRoles=$removeRoles)" } }
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: String) {
        logger.info { "start deleteUser(userId=$userId)" }
        userService.deleteUser(userId)
            .also { logger.info { "end deleteUser(userId=$userId)" } }
    }
}
