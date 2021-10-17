package cloud.carvis.backend.integration

import cloud.carvis.backend.util.AbstractApplicationTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class CarRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser
    fun `cars GET - no cars`() {
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(content().string(equalTo("[]")))
    }
}
