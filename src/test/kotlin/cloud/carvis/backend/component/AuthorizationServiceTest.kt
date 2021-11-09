package cloud.carvis.backend.component

import cloud.carvis.backend.AbstractApplicationTest
import com.github.benmanes.caffeine.cache.Cache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
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
        assertThat(cache()).hasSize(1)
        assertThat(cache()["${notExistingCarId}_foo"]).isEqualTo(false)
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
        assertThat(cache()).hasSize(2)
        assertThat(cache()["${aCarId}_foo"]).isEqualTo(false)
        assertThat(cache()["${otherCarId}_foo"]).isEqualTo(false)
    }

    private fun cache(): Map<String, Boolean> {
        val cache: Cache<String, Boolean> = cacheManager.getCache("authorization")!!.nativeCache as Cache<String, Boolean>
        return cache.asMap()
    }

}
