package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole.ADMIN
import cloud.carvis.api.users.model.UserRole.USER
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class MyUserRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(username = "j.w")
    fun `my-user GET - fetch successfully`() {
        // given
        auth0Mock.withUsers(
            UserDto(
                userId = "j.w",
                email = "j@wayne.com",
                name = "John Wayne",
                company = "Wayne Inc.",
                phone = "+1-555-555-5555",
                roles = listOf(USER, ADMIN),
                picture = "https://pic.com"
            )
        )

        // when / then
        this.mockMvc.perform(get("/my-user"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("j.w")))
            .andExpect(jsonPath("$.name", equalTo("John Wayne")))
            .andExpect(jsonPath("$.company", equalTo("Wayne Inc.")))
            .andExpect(jsonPath("$.phone", equalTo("+1-555-555-5555")))
            .andExpect(jsonPath("$.email", equalTo("j@wayne.com")))
            .andExpect(jsonPath("$.roles.length()").value(2))
            .andExpect(jsonPath("$.roles", hasItems("user", "admin")))
            .andExpect(jsonPath("$.isNewUser", equalTo(false)))
            .andExpect(jsonPath("$.picture", equalTo("https://pic.com")))
    }

    @Test
    fun `my-user GET - return unauthorized`() {
        this.mockMvc.perform(get("/my-user"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "the.user.id")
    fun `my-user PUT - success`() {
        // given
        val user = UserDto(
            userId = "the.user.id",
            name = "Updated Name",
            company = "updateCompany",
            email = "this@mail.test",
            phone = "updatedPhone"
        )
        auth0Mock
            .withUsers(
                UserDto(
                    userId = "the.user.id",
                    name = "John Wayne",
                    company = "someCompany",
                    email = "this@mail.test",
                    phone = "123 456"
                )
            )
            .withUpdateResponse(user)

        // when / then
        this.mockMvc.perform(
            put("/my-user")
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("the.user.id")))
            .andExpect(jsonPath("$.name", equalTo("Updated Name")))
            .andExpect(jsonPath("$.company", equalTo("updateCompany")))
            .andExpect(jsonPath("$.phone", equalTo("updatedPhone")))
            .andExpect(jsonPath("$.email", equalTo("this@mail.test")))
    }
}
