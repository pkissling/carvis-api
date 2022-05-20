package cloud.carvis.api.users.rest

import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
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

    @GetMapping("/users/{id}")
    fun fetchUser(@PathVariable id: String): UserDto {
        logger.info { "start fetchUser(id=$id)" }
        return userService.fetchUser(id)
            .also { logger.info { "end fetchUser(id=$id) return=${it}" } }
    }

    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: String, @Valid @RequestBody user: UserDto): UserDto {
        logger.info { "start updateUser(id=$id,user=$user)" }
        return userService.updateUser(id, user)
            .also { logger.info { "end updateUser(id=$id,user=$user), return=${it}" } }
    }

    @GetMapping("/users")
    fun fetchAllUsers(): List<UserDto> {
        logger.info { "start fetchAllUsers()" }
        return userService.fetchAllUsers()
            .also { logger.info { "end fetchAllUsers() return=${it}" } }
    }

    @PostMapping("/users/{id}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addUserRoles(@PathVariable id: String, @RequestBody addRoles: List<UserRole>) {
        logger.info { "start addUserRoles(id=$id,addRoles=$addRoles)" }
        return userService.addUserRoles(id, addRoles)
            .also { logger.info { "end addUserRoles(id=$id,addRoles=$addRoles)" } }
    }

    @DeleteMapping("/users/{id}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeUserRoles(@PathVariable id: String, @RequestBody removeRoles: List<UserRole>) {
        logger.info { "start removeUserRoles(id=$id,removeRoles=$removeRoles)" }
        userService.removeUserRoles(id, removeRoles)
            .also { logger.info { "end removeUserRoles(id=$id,removeRoles=$removeRoles)" } }
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: String) {
        logger.info { "start deleteUser(id=$id)" }
        userService.deleteUser(id)
            .also { logger.info { "end deleteUser(id=$id)" } }
    }

    @GetMapping("/new-users-count")
    fun fetchNewUsersCount(): Long {
        logger.info { "start fetchNewUsersCount()" }
        return userService.fetchNewUsersCount()
            .also { logger.info { "end fetchNewUsersCount() return=${it}" } }

    }
}
