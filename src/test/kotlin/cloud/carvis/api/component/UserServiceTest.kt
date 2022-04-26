package cloud.carvis.api.component

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_NAME
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.verify.VerificationTimes.exactly
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class UserServiceTest : AbstractApplicationTest() {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
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
            .withCar(VALID_USER_ID)
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
    @WithMockUser
    fun `cars GET - enrich username with fallback`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar("404")
            .getCar()
            .value()

        // when / then
        this.mockMvc
            .perform(get("/cars/{id}", car.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.createdBy").value("404"))
            .andExpect(jsonPath("$.ownerName").value("404"))
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `cars GET - cache username`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar(VALID_USER_ID)
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
        auth0Mock.verify(
            request()
                .withPath("/api/v2/users/a-random-user-id"), exactly(1))
    }
}
