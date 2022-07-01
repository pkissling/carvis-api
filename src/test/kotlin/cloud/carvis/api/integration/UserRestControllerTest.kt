package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.users.model.UserDto
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class UserRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser
    fun `users-userid GET - get user successfully`() {
        // given
        auth0Mock.withUsers(
            UserDto(
                userId = "someUserId",
                name = "John Wayne",
                company = "someCompany",
                email = "e@mail.com",
                phone = "123456789",
                picture = "some.pic"
            )
        )

        // when / then
        this.mockMvc.perform(get("/users/{userId}", "someUserId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("someUserId")))
            .andExpect(jsonPath("$.name", equalTo("John Wayne")))
            .andExpect(jsonPath("$.company", equalTo("someCompany")))
            .andExpect(jsonPath("$.email", equalTo("e@mail.com")))
            .andExpect(jsonPath("$.phone", equalTo("123456789")))
            .andExpect(jsonPath("$.isNewUser", equalTo(false)))
            .andExpect(jsonPath("$.picture", equalTo("some.pic")))
    }

    @Test
    @WithMockUser
    fun `users-userid GET - get user throws 404`() {
        // given
        auth0Mock.withUsers(UserDto(userId = "someUserId", name = "John Wayne", company = "someCompany"))

        // when / then
        this.mockMvc.perform(get("/users/{userId}", "404"))
            .andExpect(status().isNotFound)
    }
}
