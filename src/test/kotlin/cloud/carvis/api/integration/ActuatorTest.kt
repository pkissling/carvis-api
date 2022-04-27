package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ActuatorTest : AbstractApplicationTest() {

    @Test
    fun `actuator-health GET`() {
        this.mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", equalTo("UP")))
    }

    @Test
    @WithMockUser
    fun `actuator GET - verify exposed endpoints`() {
        this.mockMvc.perform(get("/actuator"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.length()").value(3))
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/actuator"))
            .andExpect(jsonPath("$._links.health.href").value("http://localhost/actuator/health"))
            .andExpect(jsonPath("$._links.health-path.href").value("http://localhost/actuator/health/{*path}"))
    }

    @Test
    fun `actuator GET - not accessible as unauthorized user`() {
        this.mockMvc.perform(get("/actuator"))
            .andExpect(status().isUnauthorized)
    }
}
