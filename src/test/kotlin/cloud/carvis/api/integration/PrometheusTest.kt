package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.images.model.ImageHeight
import cloud.carvis.api.users.model.UserDto
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.*


class PrometheusTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - images`() {
        // given
        testDataGenerator
            .withImage()
            .withImage()
            .withImage(height = ImageHeight.`100`)
            .withImage(height = ImageHeight.`48`)
            .withDeletedImage()

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"images\",} 2.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - requests`() {
        // given
        testDataGenerator
            .withRequest()
            .withRequest()
            .withRequest()
            .withRequest()

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"requests\",} 4.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - cars`() {
        // given
        testDataGenerator
            .withCar()

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"cars\",} 1.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - news users`() {
        // given
        testDataGenerator
            .withNewUsers(22)

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"new_users\",} 22.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - users`() {
        // given
        auth0Mock
            .withUsers(
                UserDto("id1"),
                UserDto("id2"),
                UserDto("id3"),
                UserDto("id4"),
                UserDto("id5")
            )

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"users\",} 5.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - monthly active users`() {
        // given
        auth0Mock.withActiveUsers(789)

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"monthly_active_users\",} 789.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - daily logins`() {
        // given
        auth0Mock.withDailyLogins(111)
        val date = Instant.now()
            .atZone(ZoneId.of("UTC"))
            .truncatedTo(DAYS)
            .let { DateTimeFormatter.ofPattern("YYYYMMdd").format(it) }

        // when
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"daily_logins\",} 111.0")))

        // then
        auth0Mock.verify(
            HttpRequest.request()
                .withPath("/api/v2/stats/daily")
                .withQueryStringParameter("from", date)
                .withQueryStringParameter("to", date)
                .withMethod("GET")
        )
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - 0 daily logins`() {
        // given
        auth0Mock.withDailyLogins(null)

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"daily_logins\",} 0.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - currently active users`() {
        // given
        auth0Mock.withDailyLogins(111)

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("business_objects_count{domain=\"currently_active_users\",}")))
    }


    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - technical kpi - queue messages count`() {
        // given
        testDataGenerator
            .withUserSignupEvent()
            .withUserSignupEvent()
            .withDeleteImageCommand()
            .withAssignImageToCarCommand()
            .withAssignImageToCarCommand()
            .withCarDeletedEvent(imageIds = listOf(UUID.randomUUID()))
        awaitAssert {
            assertThat(testDataGenerator.getUserSignupMessageCount()).isEqualTo(0)
            assertThat(testDataGenerator.getUserSignupDlqMessageCount()).isEqualTo(2)
            assertThat(testDataGenerator.getCarvisCommandMessageCount()).isEqualTo(0)
            assertThat(testDataGenerator.getCarvisCommandDlqMessageCount()).isEqualTo(3)
            assertThat(testDataGenerator.getCarvisEventDlqMessageCount()).isEqualTo(1)
        }

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("queue_messages_count{queue_name=\"carvis-dev-user_signup\",} 0.0")))
            .andExpect(content().string(containsString("queue_messages_count{queue_name=\"carvis-dev-user_signup_dlq\",} 2.0")))
            .andExpect(content().string(containsString("queue_messages_count{queue_name=\"carvis-dev-carvis_command\",} 0.0")))
            .andExpect(content().string(containsString("queue_messages_count{queue_name=\"carvis-dev-carvis_command_dlq\",} 3.0")))
            .andExpect(content().string(containsString("queue_messages_count{queue_name=\"carvis-dev-carvis_event\",} 0.0")))
            .andExpect(content().string(containsString("queue_messages_count{queue_name=\"carvis-dev-carvis_event_dlq\",} 1.0")))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - technical kpi - s3 deleted images count`() {
        // given
        testDataGenerator
            .withDeletedImage()
            .withDeletedImage()
            .withDeletedImage()

        // when / then
        this.mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("images_count{type=\"deleted\",} 3.0")))
    }
}
