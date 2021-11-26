package cloud.carvis.api.restclients

import com.auth0.client.mgmt.ManagementAPI
import com.auth0.json.mgmt.users.User
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class Auth0RestClient(private val managementApi: ManagementAPI) {

    private val logger = KotlinLogging.logger {}

    fun fetchUserDetails(userId: String): User? = try {
        managementApi.users()
            .get(userId, null)
            .execute()
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch userId from Auth0: $userId" }
        null
    }
}
