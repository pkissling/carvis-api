package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class HealthEndpointTest : AbstractApplicationTest() {

    @Test
    fun `actuator-health GET`() {
        this.mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", equalTo("UP")))
    }
}
