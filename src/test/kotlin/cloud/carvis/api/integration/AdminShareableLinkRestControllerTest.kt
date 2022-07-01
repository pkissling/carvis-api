package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import cloud.carvis.api.shareableLinks.model.ShareableLinkDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class AdminShareableLinkRestControllerTest : AbstractApplicationTest() {

    @Autowired
    lateinit var shareableLinkRepository: ShareableLinkRepository

    @Test
    @WithMockUser
    fun `admin-shareable-links POST - normal user yields forbidden`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "some name" } """)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-shareable-links POST - empty recipient name yields bad requests`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": " " } """)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-shareable-links POST - no UUID as carId yields bad requests`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", "no-uuid")
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "some name" } """)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-shareable-links POST - non-existing carId yields 404`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "some name" } """)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = "user123", roles = ["ADMIN"])
    fun `admin-shareable-links POST - success`() {
        // given
        val car = testDataGenerator
            .withCar()
            .getCar()
            .value()

        // when
        val result = this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", car.id)
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "John Wayne" } """)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.carId").value(car.id.toString()))
            .andExpect(jsonPath("$.recipientName").value("John Wayne"))
            .andExpect(jsonPath("$.carDetails.brand").value(car.brand))
            .andExpect(jsonPath("$.carDetails.type").value(car.type))
            .andExpect(jsonPath("$.visitorCount").value(0))
            .andExpect(jsonPath("$.createdBy").value("user123"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
            .andExpect(jsonPath("$.shareableLinkReference").isNotEmpty)
            .andReturn()
            .toObject<ShareableLinkDto>()

        // then
        assertThat(shareableLinkRepository.count()).isEqualTo(1L)
        val entity = shareableLinkRepository.findAll().first()
        assertThat(entity.shareableLinkReference).extracting { it?.value?.length }.isEqualTo(8)
        assertThat(entity).extracting { it.shareableLinkReference }.isEqualTo(result.shareableLinkReference)
        assertThat(entity).extracting { it.carId }.isEqualTo(car.id)
        assertThat(entity).extracting { it.recipientName }.isEqualTo("John Wayne")
        assertThat(entity).extracting { it.visitorCount?.get() }.isEqualTo(0L)
        assertThat(entity).extracting { it.createdAt }.isNotNull
        assertThat(entity).extracting { it.createdBy }.isEqualTo("user123")
    }
}
