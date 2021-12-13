package cloud.carvis.api.restclients

import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.RolesFilter
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

    fun fetchAllAdmins(): List<User> = try {
        val roles = managementApi.roles()
            .list(RolesFilter().withName("admin"))
            .execute()
            .items

        if (roles.size > 1) {
            throw RuntimeException("Auth0 did return more than 1 role for roleName 'admin': $roles")
        }

        val adminRole = roles.firstOrNull()
            ?: throw RuntimeException("Auth0 did return not return an roleId from role 'admin'")

        managementApi.roles()
            .listUsers(adminRole.id, null)
            .execute()
            .items
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch all admins from Auth0" }
        emptyList()
    }
}
