package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole.USER
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class NotificationRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(roles = ["USER"])
    fun `notifications-new-users-count GET - return forbidden for user with role user`() {
        // given
        testDataGenerator
            .withNewUsers(2)

        // when / then
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `notifications-new-users-count GET - return actual count`() {
        // given
        testDataGenerator
            .withNewUsers(2)

        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(2))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `notifications-new-users-count GET - dismiss new user after assigning role`() {
        // given
        val userId = UUID.randomUUID().toString()
        auth0Mock
            .withUsers(UserDto(userId = userId, roles = listOf(USER)))
            .withAddRoleResponse(userId)
        testDataGenerator
            .withNewUsers(userId)
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(1))

        // when
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", userId)
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // then
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(0))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `notifications-new-users-count GET - reduce new user count after assigning role`() {
        // given
        val userId1 = UUID.randomUUID().toString()
        val userId2 = UUID.randomUUID().toString()
        auth0Mock
            .withUsers(
                UserDto(userId = userId1, roles = listOf(USER)),
                UserDto(userId = userId2, roles = listOf(USER))
            )
            .withAddRoleResponse(userId1)
        testDataGenerator
            .withNewUsers(userId1, userId2)
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(2))

        // when
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", userId1)
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // then
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(1))
    }
}
