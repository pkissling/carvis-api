package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.common.config.Auth0Config
import cloud.carvis.api.util.mocks.Auth0Mock
import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.verify.VerificationTimes.exactly
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration


class Auth0Test : AbstractApplicationTest() {

    @Autowired
    lateinit var auth0Config: Auth0Config

    @Autowired
    lateinit var managementApi: ManagementAPI

    @Autowired
    lateinit var authApi: AuthAPI

    @Test
    fun `verify auth0 fetches auth token on startup`() {
        // when
        Auth0Config().managementApi("clientId", "clientSecret", Auth0Mock.getMockUrl(), authApi)

        // then
        auth0Mock.verify(
            request()
                .withPath("/oauth/token")
                .withMethod("POST")
        )
    }

    @Test
    fun `verify auth0 retries auth token fetch on error`() {
        // given
        auth0Mock
            .reset()
            .withApiTokenError(3)
            .withApiToken()

        // when
        auth0Config.scheduleTokenRenewal(Duration.ZERO, authApi, managementApi, Auth0Mock.getMockUrl())

        // then
        awaitAssert {
            auth0Mock.verify(
                request()
                    .withPath("/oauth/token")
                    .withMethod("POST"),
                times = exactly(4)
            )
        }
    }
}
