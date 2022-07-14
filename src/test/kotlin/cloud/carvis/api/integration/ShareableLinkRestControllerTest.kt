package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.images.model.ImageHeight
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


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
            .withShareableLink()
            .getShareableLink()
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
            .withShareableLink(car.id!!)
            .getShareableLink()
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

    @Test
    fun `shareable-links-ref-images-imageid GET - not existing ref yields 404`() {
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}", "some-ref", UUID.randomUUID())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - not existing imageId yields 404`() {
        // given
        val carId = testDataGenerator
            .withCar()
            .getCar()
            .value()
            .id
        val ref = testDataGenerator
            .withShareableLink(carId = carId!!)
            .getShareableLink()
            .value()
            .shareableLinkReference

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}", ref, UUID.randomUUID())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - not existing carId yields 410`() {
        // given
        val ref = testDataGenerator
            .withShareableLink(carId = UUID.randomUUID())
            .getShareableLink()
            .value()
            .shareableLinkReference

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}", ref, UUID.randomUUID())
        )
            .andExpect(status().isGone)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - no uuid as imageId yields 400`() {
        // given
        val ref = testDataGenerator
            .withShareableLink()
            .getShareableLink()
            .value()
            .shareableLinkReference

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}", ref, "no-uuid")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - unknown imageId yields 404`() {
        // given
        val carId = testDataGenerator
            .withCar()
            .getCar()
            .value()
            .id
        val ref = testDataGenerator
            .withShareableLink(carId!!)
            .getShareableLink()
            .value()
            .shareableLinkReference

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}", ref, UUID.randomUUID())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - illegal height`() {
        // given
        val ref = testDataGenerator
            .withShareableLink()
            .getShareableLink()
            .value()
            .shareableLinkReference


        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}?height={height}", ref, UUID.randomUUID(), "illegalHeight")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - success with explict height`() {
        // given
        val carId = testDataGenerator
            .withCar()
            .getCar()
            .value()
            .id
        val imageId = testDataGenerator
            .withImage(height = ImageHeight.`1080`)
            .getImage()
            .id
        val ref = testDataGenerator
            .withShareableLink(carId!!)
            .getShareableLink()
            .value()
            .shareableLinkReference

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}?height={height}", ref, imageId, "1080")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(imageId.toString()))
            .andExpect(jsonPath("$.height").value(1080))
            .andExpect(jsonPath("$.url").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
    }

    @Test
    fun `shareable-links-ref-images-imageid GET - success with default height`() {
        // given
        val carId = testDataGenerator
            .withCar()
            .getCar()
            .value()
            .id
        val imageId = testDataGenerator
            .withImage()
            .getImage()
            .id
        val ref = testDataGenerator
            .withShareableLink(carId!!)
            .getShareableLink()
            .value()
            .shareableLinkReference

        // when / then
        this.mockMvc.perform(
            get("/shareable-links/{ref}/images/{imageId}", ref, imageId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(imageId.toString()))
            .andExpect(jsonPath("$.height").value("ORIGINAL"))
            .andExpect(jsonPath("$.url").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
    }

}
