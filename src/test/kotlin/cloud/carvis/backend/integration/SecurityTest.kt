package cloud.carvis.backend.integration

import cloud.carvis.backend.util.AbstractApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class SecurityTest : AbstractApplicationTest() {

    @Test
    fun `POST with malicious string gets refused `() {
        this.mockMvc.perform(post("/cgi-bin/.%2e/.%2e/.%2e/.%2e/bin/sh"))
            .andExpect(status().isBadRequest)
    }
}
