package cloud.carvis.backend.integration

import cloud.carvis.backend.util.AbstractApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class CorsTest : AbstractApplicationTest() {

    @Test
    fun `OPTIONS - CORS headers added`() {
        this.mockMvc.perform(
            options("/")
                .header("Origin", "https://carvis.cloud")
                .header("Access-Control-Request-Method", "GET")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Access-Control-Allow-Origin", "https://carvis.cloud"))
            .andExpect(header().string("Access-Control-Allow-Methods", "GET,OPTIONS"))
            .andExpect(header().string("Access-Control-Max-Age", "3600"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
    }
}
