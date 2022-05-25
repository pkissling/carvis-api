package cloud.carvis.api.component

import cloud.carvis.api.AbstractApplicationTest
import com.github.benmanes.caffeine.cache.Cache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class AuthorizationServiceTest : AbstractApplicationTest() {

    @Test
    @WithMockUser(username = "foo")
    fun `car DELETE - Cache authorization information`() {
        // given
        val notExistingCarId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/cars/{id}", notExistingCarId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/cars/{id}", notExistingCarId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("cars-authorization")).hasSize(1)
        assertThat(cache("cars-authorization")["${notExistingCarId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `car DELETE - Cache authorization information - unique by carId`() {
        // given
        val aCarId = UUID.randomUUID()
        val otherCarId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/cars/{id}", aCarId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/cars/{id}", otherCarId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("cars-authorization")).hasSize(2)
        assertThat(cache("cars-authorization")["${aCarId}_foo"]).isEqualTo(false)
        assertThat(cache("cars-authorization")["${otherCarId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `request DELETE - Cache authorization information`() {
        // given
        val notRequestId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/requests/{id}", notRequestId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/requests/{id}", notRequestId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("requests-authorization")).hasSize(1)
        assertThat(cache("requests-authorization")["${notRequestId}_foo"]).isEqualTo(false)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `request DELETE - Cache authorization information - unique by requestId`() {
        // given
        val aRequestId = UUID.randomUUID()
        val otherRequestId = UUID.randomUUID()

        // when
        this.mockMvc.perform(delete("/requests/{id}", aRequestId))
            .andExpect(status().isForbidden)
        this.mockMvc.perform(delete("/requests/{id}", otherRequestId))
            .andExpect(status().isForbidden)

        // then
        assertThat(cache("requests-authorization")).hasSize(2)
        assertThat(cache("requests-authorization")["${aRequestId}_foo"]).isEqualTo(false)
        assertThat(cache("requests-authorization")["${otherRequestId}_foo"]).isEqualTo(false)
    }

    private fun cache(key: String): Map<String, Boolean> {
        val cache: Cache<String, Boolean> = cacheManager.getCache(key)!!.nativeCache as Cache<String, Boolean>
        return cache.asMap()
    }

}
