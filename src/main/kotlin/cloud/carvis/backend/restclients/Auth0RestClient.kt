package cloud.carvis.backend.restclients

import com.auth0.client.mgmt.ManagementAPI
import com.auth0.json.mgmt.users.User
import org.springframework.stereotype.Service

@Service
class Auth0RestClient(private val managementApi: ManagementAPI) {

    fun fetchUserDetails(userId: String): User? =
        managementApi.users()
            .get(userId, null)
            .execute()

}
