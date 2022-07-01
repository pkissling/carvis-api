package cloud.carvis.api.notifications.rest

import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationRestController(
    private val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/new-users-count")
    @PreAuthorize("@authorization.isAdmin()")
    fun fetchNewUsersCount(): Int {
        logger.info { "start fetchNewUsersCount()" }
        return userService.newUsersCount()
            .also { logger.info { "end fetchNewUsersCount() return=${it}" } }

    }
}
