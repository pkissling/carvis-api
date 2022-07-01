package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.common.clients.UserWithRoles
import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.users.model.UserRole.ADMIN
import cloud.carvis.api.users.model.UserRole.USER
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.JsonBody.json
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class AdminUserRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(username = "foo")
    fun `admin-users PUT - update other user is forbidden`() {
        // given
        val user = UserDto("bar", name = "Updated Name", company = "updateCompany")

        // when / then
        this.mockMvc.perform(
            put("/admin/users/{id}", "bar")
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "adminUser", roles = ["ADMIN"])
    fun `admin-users PUT - update other user as admin`() {
        // given
        val user = UserDto(
            userId = "a.user.id",
            name = "Updated Name",
            company = "updateCompany",
            phone = "updatedPhone",
            email = "this@mail.rocks"
        )
        auth0Mock
            .withUsers(
                UserDto(
                    userId = "a.user.id",
                    name = "John Wayne",
                    company = "someCompany",
                    email = "this@mail.rocks",
                    phone = "123 456"
                )
            )
            .withUpdateResponse(user)

        // when / then
        this.mockMvc.perform(
            put("/admin/users/{id}", "a.user.id")
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo("a.user.id")))
            .andExpect(jsonPath("$.name", equalTo("Updated Name")))
            .andExpect(jsonPath("$.company", equalTo("updateCompany")))
            .andExpect(jsonPath("$.phone", equalTo("updatedPhone")))
            .andExpect(jsonPath("$.email", equalTo("this@mail.rocks")))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users PUT - updates cache after update`() {
        // given
        val user = UserDto(
            userId = "a.user.id",
            name = "Updated Name",
            company = "updateCompany",
            phone = "updatedPhone",
            email = "this@mail.rocks"
        )
        auth0Mock
            .withUsers(
                UserDto(
                    userId = "a.user.id",
                    name = "John Wayne",
                    company = "someCompany",
                    email = "this@mail.rocks",
                    phone = "123 456"
                )
            )
            .withUpdateResponse(user)
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk)
        this.mockMvc.perform(get("/admin/users/{userId}", "a.user.id"))
            .andExpect(status().isOk)
        assertThat((cacheManager.getCache("auth0-users")?.get("a.user.id")?.get() as UserWithRoles).user.name, equalTo("John Wayne"))

        // when
        this.mockMvc.perform(
            put("/admin/users/{id}", "a.user.id")
                .content(objectMapper.writeValueAsString(user))
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // then
        assertThat((cacheManager.getCache("auth0-users")?.get("a.user.id")?.get() as UserWithRoles).user.name, equalTo("Updated Name"))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `admin-users GET - users receives forbidden response`() {
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users GET - returns list of users for admins`() {
        // given
        auth0Mock.withUsers(
            UserDto(userId = "userId1", name = "Name 1", email = "e@mail.1", phone = "+1", company = "comp1", roles = listOf(ADMIN, USER)),
            UserDto(
                userId = "userId2",
                name = "Name 2",
                email = "e@mail.2",
                phone = "+2",
                company = "comp2",
                roles = listOf(USER),
                picture = "pix"
            ),
            UserDto(userId = "userId3", name = "Name 3", email = "e@mail.3", phone = "+3", company = "comp3")
        )

        // when / then
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(3)))
            .andExpect(jsonPath("$[0].userId", equalTo("userId1")))
            .andExpect(jsonPath("$[0].name", equalTo("Name 1")))
            .andExpect(jsonPath("$[0].email", equalTo("e@mail.1")))
            .andExpect(jsonPath("$[0].phone", equalTo("+1")))
            .andExpect(jsonPath("$[0].company", equalTo("comp1")))
            .andExpect(jsonPath("$[0].roles.length()").value(2))
            .andExpect(jsonPath("$[0].roles", hasItems("user", "admin")))
            .andExpect(jsonPath("$[1].userId", equalTo("userId2")))
            .andExpect(jsonPath("$[1].name", equalTo("Name 2")))
            .andExpect(jsonPath("$[1].email", equalTo("e@mail.2")))
            .andExpect(jsonPath("$[1].phone", equalTo("+2")))
            .andExpect(jsonPath("$[1].company", equalTo("comp2")))
            .andExpect(jsonPath("$[1].roles.length()").value(1))
            .andExpect(jsonPath("$[1].roles", hasItems("user")))
            .andExpect(jsonPath("$[1].picture", equalTo("pix")))
            .andExpect(jsonPath("$[2].userId", equalTo("userId3")))
            .andExpect(jsonPath("$[2].name", equalTo("Name 3")))
            .andExpect(jsonPath("$[2].email", equalTo("e@mail.3")))
            .andExpect(jsonPath("$[2].phone", equalTo("+3")))
            .andExpect(jsonPath("$[2].company", equalTo("comp3")))
            .andExpect(jsonPath("$[2].roles.length()").value(0))
    }

    @Test
    @WithMockUser
    fun `admin-users-id-roles PUT - users receives forbidden response`() {
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", "some.userId")
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles PUT - bad request`() {
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", "some.userId")
                .content("[]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles PUT - no roles yields bad request`() {
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", "some.userId")
                .content("[]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles PUT - invalid role yields bad request`() {
        // given
        auth0Mock.withUsers(UserDto(userId = "userId", name = "Name", roles = listOf(USER)))

        // when / then
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", "some.userId")
                .content("[\"foobar\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles PUT - success`() {
        // given
        auth0Mock
            .withUsers(UserDto(userId = "d.joe", name = "Name", roles = listOf(USER)))
            .withAddRoleResponse("d.joe")

        // when
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", "d.joe")
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // then
        auth0Mock
            .verify(
                request()
                    .withPath("/api/v2/roles")
                    .withMethod("GET")
                    .withQueryStringParameter("name_filter", "user")
            )
            .verify(
                request()
                    .withPath("/api/v2/users/d.joe/roles")
                    .withMethod("POST")
                    .withBody(json("""{ "roles" : [ "id_user" ] }"""))
            )
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles PUT - evicts cache after update`() {
        // given
        auth0Mock
            .withUsers(UserDto(userId = "d.joe", name = "Name", roles = listOf(USER)))
            .withAddRoleResponse("d.joe")
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk)
        this.mockMvc.perform(get("/admin/users/{userId}", "d.joe"))
            .andExpect(status().isOk)
        assertThat(cacheManager.getCache("auth0-users")?.get("d.joe"), notNullValue())

        // when
        this.mockMvc.perform(
            post("/admin/users/{id}/roles", "d.joe")
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // then
        assertThat(cacheManager.getCache("auth0-users")?.get("d.joe"), nullValue())
    }

    @Test
    @WithMockUser
    fun `admin-users-id-roles DELETE - users receives forbidden response`() {
        this.mockMvc.perform(
            delete("/admin/users/{id}/roles", "some.userId")
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles DELETE - bad request`() {
        this.mockMvc.perform(
            delete("/admin/users/{id}/roles", "some.userId")
                .content("[]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles DELETE - no roles yields bad request`() {
        this.mockMvc.perform(
            delete("/admin/users/{id}/roles", "some.userId")
                .content("[]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles DELETE - invalid role yields bad request`() {
        // given
        auth0Mock.withUsers(UserDto(userId = "userId", name = "Name", roles = listOf(USER)))

        // when / then
        this.mockMvc.perform(
            delete("/admin/users/{id}/roles", "some.userId")
                .content("[\"foobar\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles DELETE - success`() {
        // given
        auth0Mock
            .withRoles(UserDto(userId = "d.joe", name = "Name", roles = listOf(USER)))
            .withRemoveRoleResponse("d.joe")

        // when
        this.mockMvc.perform(
            delete("/admin/users/{id}/roles", "d.joe")
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // then
        auth0Mock
            .verify(
                request()
                    .withPath("/api/v2/roles")
                    .withMethod("GET")
                    .withQueryStringParameter("name_filter", "user")
            )
            .verify(
                request()
                    .withPath("/api/v2/users/d.joe/roles")
                    .withMethod("DELETE")
                    .withBody(json("""{ "roles" : [ "id_user" ] }"""))
            )
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users-id-roles DELETE - evicts cache after update`() {
        // given
        auth0Mock
            .withUsers(UserDto(userId = "d.joe", name = "Name", roles = listOf(USER)))
            .withRemoveRoleResponse("d.joe")
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk)
        this.mockMvc.perform(get("/admin/users/{userId}", "d.joe"))
            .andExpect(status().isOk)
        assertThat(cacheManager.getCache("auth0-users")?.get("d.joe"), notNullValue())

        // when
        this.mockMvc.perform(
            delete("/admin/users/{id}/roles", "d.joe")
                .content("[\"user\"]")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // then
        assertThat(cacheManager.getCache("auth0-users")?.get("d.joe"), nullValue())
    }

    @Test
    @WithMockUser
    fun `admin-users DELETE - users receives forbidden response`() {
        this.mockMvc.perform(
            delete("/admin/users/{id}", "some.userId")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users DELETE - success`() {
        // given
        auth0Mock.withDeleteUserResponse("usr")

        // when
        this.mockMvc.perform(
            delete("/admin/users/{id}", "usr")
        )
            .andExpect(status().isOk)

        // then
        auth0Mock.verify(
            request()
                .withPath("/api/v2/users/usr")
                .withMethod("DELETE")
        )
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users DELETE - evicts cache after delete`() {
        // given
        auth0Mock
            .withUsers(UserDto(userId = "d.joe", name = "Name", roles = listOf(USER)))
            .withDeleteUserResponse("d.joe")
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk)
        this.mockMvc.perform(get("/admin/users/{userId}", "d.joe"))
            .andExpect(status().isOk)
        assertThat(cacheManager.getCache("auth0-users")?.get("d.joe"), notNullValue())

        // when
        this.mockMvc.perform(
            delete("/admin/users/{id}", "d.joe")
        )
            .andExpect(status().isOk)

        // then
        assertThat(cacheManager.getCache("auth0-users")?.get("d.joe"), nullValue())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users DELETE - returns 404 if user not found`() {
        this.mockMvc.perform(delete("/admin/users/{userId}", "404"))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-users GET - with new and not new user`() {
        // given
        auth0Mock.withUsers(
            UserDto(userId = "userId1"),
            UserDto(userId = "userId2")
        )
        testDataGenerator.withNewUsers("userId2")

        // when / then
        this.mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(2)))
            .andExpect(jsonPath("$[0].userId", equalTo("userId1")))
            .andExpect(jsonPath("$[0].isNewUser", equalTo(false)))
            .andExpect(jsonPath("$[1].userId", equalTo("userId2")))
            .andExpect(jsonPath("$[1].isNewUser", equalTo(true)))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `users-id DELETE - decrement new users counter`() {
        // given
        auth0Mock.withDeleteUserResponse("rnd.id")
        testDataGenerator
            .withNewUsers("rnd.id")
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(1))

        // when
        this.mockMvc.perform(delete("/admin/users/{id}", "rnd.id"))
            .andExpect(status().isOk)

        // then
        this.mockMvc.perform(get("/notifications/new-users-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(0))
    }
}
