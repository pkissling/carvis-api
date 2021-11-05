package cloud.carvis.backend.component

import cloud.carvis.backend.AbstractApplicationTest
import cloud.carvis.backend.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.backend.AbstractApplicationTest.Users.VALID_USER_NAME
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class UserServiceTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun beforeEach() {
        cacheManager.cacheNames
            .mapNotNull { cacheManager.getCache(it) }
            .forEach { it.clear() }
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `cars GET - enrich username`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar()
            .setCreatedBy(VALID_USER_ID)
            .getCar()
            .value()

        // when / then
        this.mockMvc
            .perform(get("/cars/{id}", car.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.createdBy").value(VALID_USER_ID))
            .andExpect(jsonPath("$.ownerName").value(VALID_USER_NAME))
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `cars GET - cache username`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar()
            .setCreatedBy(VALID_USER_ID)
            .getCar()
            .value()

        // when
        this.mockMvc
            .perform(get("/cars/{id}", car.id))
            .andExpect(status().isOk)
        this.mockMvc
            .perform(get("/cars/{id}", car.id))
            .andExpect(status().isOk)

        // then
        verify(auth0RestClient, times(1)).fetchUserDetails(VALID_USER_ID)
    }
}
