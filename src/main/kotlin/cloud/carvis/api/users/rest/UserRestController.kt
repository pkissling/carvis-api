package cloud.carvis.api.users.rest

import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserRestController(
    val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{userId}")
    fun fetchUser(@PathVariable userId: String): UserDto {
        logger.info { "start fetchUser(userId=$userId)" }
        return userService.fetchUser(userId)
            .also { logger.info { "end fetchUser(userId=$userId) return=${it}" } }
    }
}
