package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class RequestRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser
    fun `requests GET - no requests`() {
        // given
        testDataGenerator.withEmptyDb()

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser
    fun `requests GET - with requests`() {
        // given
        testDataGenerator
            .withEmptyDb()
            .withRequest()
            .withRequest()

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @WithMockUser
    fun `request GET - success`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest()
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", request.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(request.id.toString()))
            .andExpect(jsonPath("createdAt").value(request.createdAt.toString()))
            .andExpect(jsonPath("createdBy").value(request.createdBy))
            .andExpect(jsonPath("updatedAt").value(request.updatedAt.toString()))
            .andExpect(jsonPath("updatedBy").value(request.updatedBy))
    }

    @Test
    @WithMockUser
    fun `request GET - not found`() {
        // given
        testDataGenerator.withEmptyDb()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound)
    }


}
