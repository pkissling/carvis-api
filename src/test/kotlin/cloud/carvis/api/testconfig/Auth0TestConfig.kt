package cloud.carvis.api.testconfig

import com.auth0.client.HttpOptions
import com.auth0.client.mgmt.ManagementAPI
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class Auth0TestConfig {

    @Bean
    fun managementApi(): ManagementAPI =
        ManagementAPI("dummy", "dummy", HttpOptions())
}
