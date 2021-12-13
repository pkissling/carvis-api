package cloud.carvis.api.service

import cloud.carvis.api.restclients.Auth0RestClient
import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(private val auth0RestClient: Auth0RestClient) {

    private val logger = KotlinLogging.logger {}

    @Cacheable("userNames", sync = true)
    fun fetchName(userId: String): String? =
        auth0RestClient.fetchUserDetails(userId)
            ?.name
            .also { logger.debug { "Resolved name [$it] for userId: $userId" } }

    fun fetchAllAdminEmails(): List<String> {
        val admins = auth0RestClient.fetchAllAdmins()
        if (admins.isEmpty()) {
            throw RuntimeException("No users with admin role returned by Auth0.")
        }

        val adminsWithoutEmail = admins
            .filter { it.email.isNullOrBlank() }

        if (adminsWithoutEmail.isNotEmpty()) {
            logger.warn { "Following admins have no email address set in Auth0: $adminsWithoutEmail" }
        }

        return admins
            .map { it.email }
            .also { logger.debug { "Resolved following admin emails: $it" } }
    }

}
