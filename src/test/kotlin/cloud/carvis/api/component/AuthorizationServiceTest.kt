package cloud.carvis.api.component

import cloud.carvis.api.AbstractApplicationTest
import com.github.benmanes.caffeine.cache.Cache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class AuthorizationServiceTest : AbstractApplicationTest() {

    @Autowired
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun beforeEach() {
        cacheManager.cacheNames
            .mapNotNull { cacheManager.getCache(it) }
            .forEach { it.clear() }
    }

    @Test
    @WithMockUser(username = "foo")
    fun `car DELETE - Cache authorization information`() {
        // given
        testDataGenerator.withEmptyDb()
        val notExistingCarId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/cars/{id}", notExistingCarId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/cars/{id}", notExistingCarId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("carsAuthorization")).hasSize(1)
        assertThat(cache("carsAuthorization")["${notExistingCarId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `car DELETE - Cache authorization information - unique by carId`() {
        // given
        testDataGenerator.withEmptyDb()
        val aCarId = UUID.randomUUID()
        val otherCarId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/cars/{id}", aCarId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/cars/{id}", otherCarId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("carsAuthorization")).hasSize(2)
        assertThat(cache("carsAuthorization")["${aCarId}_foo"]).isEqualTo(false)
        assertThat(cache("carsAuthorization")["${otherCarId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `request DELETE - Cache authorization information`() {
        // given
        testDataGenerator.withEmptyDb()
        val notRequestId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/requests/{id}", notRequestId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/requests/{id}", notRequestId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("requestsAuthorization")).hasSize(1)
        assertThat(cache("requestsAuthorization")["${notRequestId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `request DELETE - Cache authorization information - unique by requestId`() {
        // given
        testDataGenerator.withEmptyDb()
        val aRequestId = UUID.randomUUID()
        val otherRequestId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/requests/{id}", aRequestId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/requests/{id}", otherRequestId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("requestsAuthorization")).hasSize(2)
        assertThat(cache("requestsAuthorization")["${aRequestId}_foo"]).isEqualTo(false)
        assertThat(cache("requestsAuthorization")["${otherRequestId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `users GET - Cache authorization information`() {
        // given
        testDataGenerator.withEmptyDb()
        val userId = UUID.randomUUID()

        // when
        this.mockMvc.perform(get("/users/{id}", userId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(get("/users/{id}", userId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("usersAuthorization")).hasSize(1)
        assertThat(cache("usersAuthorization")["${userId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `users GET - Cache authorization information - unique by userId`() {
        // given
        testDataGenerator.withEmptyDb()
        val aUserId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()

        // when
        this.mockMvc.perform(get("/users/{id}", aUserId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(get("/users/{id}", otherUserId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("usersAuthorization")).hasSize(2)
        assertThat(cache("usersAuthorization")["${aUserId}_foo"]).isEqualTo(false)
        assertThat(cache("usersAuthorization")["${otherUserId}_foo"]).isEqualTo(false)
    }

    private fun cache(key: String): Map<String, Boolean> {
        val cache: Cache<String, Boolean> = cacheManager.getCache(key)!!.nativeCache as Cache<String, Boolean>
        return cache.asMap()
    }

}
