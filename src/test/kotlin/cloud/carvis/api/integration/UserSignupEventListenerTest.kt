package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.SesHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class UserSignupEventListenerTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var sesHelper: SesHelper

    @Test
    fun `onMessage - success`() {
        // given
        auth0Mock.withRole("admin", roleId = "rol_1283injasd")
        auth0Mock.withUser("auth0|ijasdjiasdai", email = "dummy@dummy.com")
        auth0Mock.withUserRoleAssignment("rol_1283injasd", "dummy@dummy.com")

        // when
        testDataGenerator.withUserSignupEvent()

        // then
        val mail = sesHelper.firstMail()
        assertThat(mail.read<String>("Source")).isEqualTo("usersignup@carvis.cloud")
        assertThat(mail.read<List<String>>("Destinations.ToAddresses")).hasSize(1)
        assertThat(mail.read<List<String>>("Destinations.ToAddresses")[0]).isEqualTo("dummy@dummy.com")
        assertThat(mail.read<List<String>>("Destinations.CcAddresses")).isEmpty()
        assertThat(mail.read<List<String>>("Destinations.BccAddresses")).isEmpty()
        assertThat(mail.read<String>("Subject")).isEqualTo("hi")
        assertThat(mail.read<String>("Body")).isEqualTo("text")
    }
}
