package cloud.carvis.api.util.helpers

import cloud.carvis.api.util.mocks.Auth0Mock
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils

class Auth0PropertyOverrideContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            configurableApplicationContext, "auth.domain=${Auth0Mock.getMockUrl()}"
        )
    }
}
