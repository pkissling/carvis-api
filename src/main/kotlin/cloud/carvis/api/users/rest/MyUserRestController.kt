package cloud.carvis.api.users.rest

import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/my-user")
class MyUserRestController(
    val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun fetchOwnUser(): UserDto {
        logger.info { "start fetchOwnUser()" }
        return userService.fetchOwnUser()
            .also { logger.info { "end fetchOwnUser() return=${it}" } }
    }

    @PutMapping
    fun updateOwnUser(@Valid @RequestBody user: UserDto): UserDto {
        logger.info { "start updateOwnUser(user=$user)" }
        return userService.updateOwnUser(user)
            .also { logger.info { "end updateOwnUser(user=$user), return=${it}" } }
    }
}
