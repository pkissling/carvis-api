package cloud.carvis.api.common.filters

import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
class ActiveUsersFilter(private val userService: UserService) : Filter {

    val logger = KotlinLogging.logger {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val isActualUser = SecurityContextHolder.getContext().authentication.authorities
            .map { it.authority }
            .mapNotNull { UserRole.from(it) }
            .any { it.isActualUser() }

        if (isActualUser) {
            val userName = SecurityContextHolder.getContext().authentication?.name ?: "n/a"
            val added = userService.addCurrentlyActiveUser(userName)
            if (added) {
                logger.info { "Registered new active user: $userName" }
            }
        }

        chain.doFilter(request, response)
    }
}
