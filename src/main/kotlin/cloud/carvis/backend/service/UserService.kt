package cloud.carvis.backend.service

import cloud.carvis.backend.restclients.Auth0RestClient
import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(private val auth0RestClient: Auth0RestClient) {

    private val logger = KotlinLogging.logger {}

    @Cacheable("userNames", sync = true)
    fun fetchName(userId: String): String? = try {
        auth0RestClient.fetchUserDetails(userId)
            ?.name
            .also { logger.debug { "Resolved name [$it] for userId: $userId" } }
    } catch (e: Error) {
        logger.warn(e) { "Unable to fetch name for userId: $userId" }
        null
    }
}
