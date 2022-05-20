package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.users.model.UserDto
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class PrometheusTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(roles = ["SYSTEM"])
    fun `actuator-prometheus GET - business kpi - images`() {
        // given
        testDataGenerator
            .withImage()
            .withImage()
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
}
