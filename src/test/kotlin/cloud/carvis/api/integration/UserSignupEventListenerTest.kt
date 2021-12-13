package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.util.helpers.SesHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class UserSignupEventListenerTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var sesHelper: SesHelper

    @Test
    fun `onMessage - success`() {
        // given
        auth0Mock
            .withRole("admin", roleId = "rol_1283injasd")
            .withUserRoleAssignment("rol_1283injasd", "dummy@dummy.com")

        // when
        val event = testDataGenerator
            .withUserSignupEvent()
            .getUserSignupEvent()
            .value()

        // then
        val mail = sesHelper.firstMail()
        assertThat(mail.read<String>("Source")).isEqualTo("usersignup@carvis.cloud")
        assertThat(mail.read<List<String>>("Destinations.ToAddresses")).hasSize(1)
        assertThat(mail.read<List<String>>("Destinations.ToAddresses")[0]).isEqualTo("dummy@dummy.com")
        assertThat(mail.read<List<String>>("Destinations.CcAddresses")).isEmpty()
        assertThat(mail.read<List<String>>("Destinations.BccAddresses")).isEmpty()
        assertThat(mail.read<String>("Subject")).isEqualTo("Neuer Nutzer")
        assertThat(mail.read<String>("Body")).isEqualTo(
            """
            Hi,

            ein neuer Nutzer hat sich registiert:
            
            User-ID: ${event.userId}
            E-Mail: ${event.email}
            Name: ${event.name}
            Benutzername: ${event.username}

            Bitte gib dem Nutzer die entsprechende Berechtigung: https://manage.auth0.com/dashboard/eu/carvis/roles/rol_LIjMXu4DezolQ21w/users
            """.trimIndent()
        )
    }
}
