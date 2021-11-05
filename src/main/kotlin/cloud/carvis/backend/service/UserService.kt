package cloud.carvis.backend.service

import cloud.carvis.backend.restclients.Auth0RestClient
import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(private val auth0RestClient: Auth0RestClient) {

    private val logger = KotlinLogging.logger {}

    @Cacheable("userDetails", sync = true)
    fun fetchUsername(userId: String): String? = try {
        auth0RestClient.fetchUserDetails(userId)
            ?.username
    } catch (e: Error) {
        logger.warn(e) { "Unable to fetch username for userId: $userId" }
        null
    }
}
