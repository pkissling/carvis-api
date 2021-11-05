package cloud.carvis.backend.integration

import cloud.carvis.backend.AbstractApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class CorsTest : AbstractApplicationTest() {

    @Test
    fun `OPTIONS - CORS headers added`() {
        this.mockMvc.perform(
            options("/")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
            .andExpect(header().string("Access-Control-Allow-Methods", "OPTIONS,GET,POST,PUT,DELETE"))
            .andExpect(header().string("Access-Control-Max-Age", "3600"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
    }
}
