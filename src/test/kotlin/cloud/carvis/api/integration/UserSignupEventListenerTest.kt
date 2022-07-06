package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.users.dao.NewUserRepository
import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.util.helpers.SesHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.springframework.beans.factory.annotation.Autowired


class UserSignupEventListenerTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var newUserRepository: NewUserRepository

    @Autowired
    private lateinit var sesHelper: SesHelper

    @Test
    fun `onMessage - send mail to admin`() {
        // given
        auth0Mock
            .withUsers(
                UserDto(
                    userId = "user-id",
                    name = "na-me",
                    company = "com-pany",
                    phone = "ph-one",
                    email = "dummy@dummy.com",
                    roles = listOf(UserRole.ADMIN)
                )
            )

        // when
        val event = testDataGenerator
            .withUserSignupEvent()
            .getUserSignupEvent()
            .value()

        // then
        val mail = sesHelper.latestMail()
        assertThat(mail.read<String>("Source")).isEqualTo("usersignup@carvis.cloud")
        assertThat(mail.read<List<String>>("Destination.ToAddresses")).hasSize(1)
        assertThat(mail.read<List<String>>("Destination.ToAddresses")[0]).isEqualTo("dummy@dummy.com")
        assertThat(mail.read<String>("Subject")).isEqualTo("Neuer Nutzer")
        assertThat(mail.read<String>("Body.text_part")).isEqualTo(
            """
            Hi,

            ein neuer Nutzer hat sich registiert:
            
            User-ID: ${event.userId}
            E-Mail: ${event.email}
            Name: ${event.name}

            Bitte gib dem Nutzer die entsprechende Berechtigung: https://carvis.cloud/user-management
            """.trimIndent()
        )
    }

    @Test
    fun `onMessage - increase new users counter from 0 to 1`() {
        // given
        auth0Mock
            .withUsers(
                UserDto(
                    userId = "user-id",
                    name = "na-me",
                    company = "com-pany",
                    phone = "ph-one",
                    email = "dummy@dummy.com",
                    roles = listOf(UserRole.ADMIN)
                )
            )

        // when
        val event = testDataGenerator
            .withUserSignupEvent()
            .getUserSignupEvent()
            .value()

        // then
        awaitAssert {
            assertThat(newUserRepository.count()).isEqualTo(1L)
            assertThat(newUserRepository.findByHashKey(event.userId)).isNotNull
        }
    }

    @Test
    fun `onMessage - increase new users counter from 1 to 2`() {
        // given
        auth0Mock
            .withUsers(
                UserDto(
                    userId = "user-id",
                    name = "na-me",
                    company = "com-pany",
                    phone = "ph-one",
                    email = "dummy@dummy.com",
                    roles = listOf(UserRole.ADMIN)
                )
            )

        // when
        val eventOne = testDataGenerator
            .withUserSignupEvent()
            .getUserSignupEvent()
            .value()
        val eventTwo = testDataGenerator
            .withUserSignupEvent()
            .getUserSignupEvent()
            .value()

        // then
        awaitAssert {
            assertThat(newUserRepository.findAll().count()).isEqualTo(2)
            assertThat(newUserRepository.findByHashKey(eventTwo.userId)).isNotNull
            assertThat(newUserRepository.findByHashKey(eventOne.userId)).isNotNull
        }
    }

    @Test
    fun `onMessage - processing error`() {
        // given
        auth0Mock
            .withRolesError(
                mapOf("name_filter" to "admin")
            )

        // when
        val event = testDataGenerator
            .withUserSignupEvent()
            .getUserSignupEvent()
            .value()

        // then
        awaitAssert {
            assertThat(testDataGenerator.getUserSignupDlqMessageCount()).isEqualTo(1)
            assertThat(newUserRepository.count()).isEqualTo(1L)
            assertThat(newUserRepository.existsByHashKey(event.userId)).isTrue
        }
        auth0Mock.verify(
            request()
                .withPath("/api/v2/roles")
                .withQueryStringParameter("name_filter", "admin")
                .withMethod("GET")
        )
    }
}
