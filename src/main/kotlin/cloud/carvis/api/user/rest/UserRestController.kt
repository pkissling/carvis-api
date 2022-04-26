package cloud.carvis.api.user.rest

import cloud.carvis.api.user.model.UserDto
import cloud.carvis.api.user.service.UserService
import mu.KotlinLogging
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
}