package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
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
    @WithMockUser(username = "foo")
    fun `request GET - success`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", request.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(request.id.toString()))
            .andExpect(jsonPath("createdAt").value(request.createdAt.toString()))
            .andExpect(jsonPath("createdBy").value(request.createdBy))
            .andExpect(jsonPath("hasHiddenFields").value(false))
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

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `requests GET - admin can read all fields`() {
        // given
        testDataGenerator
            .withEmptyDb()
            .withRequest()
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].hasHiddenFields").value(false))
    }

    @Test
    @WithMockUser(username = "bar")
    fun `request GET - with hidden fields`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", request.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(request.id.toString()))
            .andExpect(jsonPath("createdAt").value(request.createdAt.toString()))
            .andExpect(jsonPath("createdBy").value(request.createdBy))
            .andExpect(jsonPath("hasHiddenFields").value(true))
            .andExpect(jsonPath("updatedAt").value(request.updatedAt.toString()))
            .andExpect(jsonPath("updatedBy").value(request.updatedBy))
    }

    @Test
    @WithMockUser(username = "bar")
    fun `requests GET - with some hidden fields`() {
        // given
        testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .withRequest("bar")

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[?(@.createdBy=='foo')].hasHiddenFields").value(true))
            .andExpect(jsonPath("$[?(@.createdBy=='bar')].hasHiddenFields").value(false))
    }

    @Test
    @WithMockUser(username = "foo")
    fun `request DELETE - success`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .getRequest().value()

        // when
        this.mockMvc.perform(delete("/requests/{id}", request.id))
            .andExpect(status().isNoContent)

        // then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

}
