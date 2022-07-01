package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class ShareableLinkRestControllerTest : AbstractApplicationTest() {

    @Autowired
    lateinit var shareableLinkRepository: ShareableLinkRepository

    @Test
    fun `shareable-links-ref-car GET - not existing ref yields 404`() {
        this.mockMvc.perform(
            get("/shareable-links/{ref}/car", "some-ref")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `shareable-links-ref-car GET - not existing car yields 400`() {
        // given
        val shareableLink = testDataGenerator
            .withSharedLinkReference()
            .getSharedLinkReference()
            .value()

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/car", shareableLink.shareableLinkReference)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `shareable-links-ref-car GET - success`() {
        // given
        val car = testDataGenerator
            .withCar()
            .getCar()
            .value()
        val shareableLink = testDataGenerator
            .withSharedLinkReference(car.id!!)
            .getSharedLinkReference()
            .value()
        val visitorCount = shareableLink.visitorCount!!.get()

        // when
        this.mockMvc.perform(
            get("/shareable-links/{ref}/car", shareableLink.shareableLinkReference)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(car.id.toString()))

        // then
        awaitAssert {
            assertThat(shareableLinkRepository.findByHashKey(shareableLink.shareableLinkReference!!))
                .extracting { it?.visitorCount?.get() }
                .isEqualTo(visitorCount + 1)
        }
    }
}
