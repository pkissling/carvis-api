package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.user.model.UserDto
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class UserRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(username = "someUserId")
    fun `users GET - get own user`() {
        // given
        testDataGenerator.withEmptyDb()
        auth0Mock.withUser("someUserId", name = "John Wayne", company = "someCompany")

        // when / then
        this.mockMvc.perform(get("/users/{userId}", "someUserId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("someUserId")))
            .andExpect(jsonPath("$.name", equalTo("John Wayne")))
            .andExpect(jsonPath("$.company", equalTo("someCompany")))
    }

    @Test
    @WithMockUser(username = "foo")
    fun `users GET - access other user is forbidden`() {
        // given
        testDataGenerator.withEmptyDb()
        auth0Mock.withUser("bar")

        // when / then
        this.mockMvc.perform(get("/users/{userId}", "bar"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "adminUserId", roles = ["ADMIN"])
    fun `users GET - access other user as admin`() {
        // given
        testDataGenerator.withEmptyDb()
        auth0Mock.withUser("regularUserId")

        // when / then
        this.mockMvc.perform(get("/users/{userId}", "regularUserId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("regularUserId")))
    }

    @Test
    @WithMockUser(username = "someUserId")
    fun `users PUT - update own user`() {
        // given
        testDataGenerator.withEmptyDb()
        val user = UserDto("someUserId", name = "Updated Name", company = "updateCompany")
        auth0Mock
            .withUser("someUserId", name = "John Wayne", company = "someCompany")
            .withUpdateResponse(user)

        // when / then
        this.mockMvc.perform(
            put("/users/{id}", user.userId)
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("someUserId")))
            .andExpect(jsonPath("$.name", equalTo("Updated Name")))
            .andExpect(jsonPath("$.company", equalTo("updateCompany")))
    }

    @Test
    @WithMockUser(username = "foo")
    fun `users PUT - update other user is forbidden`() {
        // given
        testDataGenerator.withEmptyDb()
        val user = UserDto("bar", name = "Updated Name", company = "updateCompany")

        // when / then
        this.mockMvc.perform(
            put("/users/{id}", "bar")
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "adminUser", roles = ["ADMIN"])
    fun `users PUT - update other user as admin`() {
        // given
        testDataGenerator.withEmptyDb()
        val user = UserDto("someUserId", name = "Updated Name", company = "updateCompany")
        auth0Mock
            .withUser("someUserId", name = "John Wayne", company = "someCompany")
            .withUpdateResponse(user)

        // when / then
        this.mockMvc.perform(
            put("/users/{id}", "someUserId")
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("someUserId")))
            .andExpect(jsonPath("$.name", equalTo("Updated Name")))
            .andExpect(jsonPath("$.company", equalTo("updateCompany")))
    }
}
