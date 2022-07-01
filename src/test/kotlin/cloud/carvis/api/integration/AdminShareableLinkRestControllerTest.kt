package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import cloud.carvis.api.shareableLinks.model.ShareableLinkDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class AdminShareableLinkRestControllerTest : AbstractApplicationTest() {

    @Autowired
    lateinit var shareableLinkRepository: ShareableLinkRepository

    @Test
    @WithMockUser
    fun `admin-cars-carid-shareable-links POST - normal user yields forbidden`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "some name" } """)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-cars-carid-shareable-links POST - empty recipient name yields bad requests`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": " " } """)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-cars-carid-shareable-links POST - no UUID as carId yields bad requests`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", "no-uuid")
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "some name" } """)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-cars-carid-shareable-links POST - non-existing carId yields 404`() {
        this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "some name" } """)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = "user123", roles = ["ADMIN"])
    fun `admin-cars-carid-shareable-links POST - success`() {
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
        assertThat(entity.shareableLinkReference?.length).isEqualTo(8)
        assertThat(entity).extracting { it.shareableLinkReference }.isEqualTo(result.shareableLinkReference)
        assertThat(entity).extracting { it.carId }.isEqualTo(car.id)
        assertThat(entity).extracting { it.recipientName }.isEqualTo("John Wayne")
        assertThat(entity).extracting { it.visitorCount?.get() }.isEqualTo(0L)
        assertThat(entity).extracting { it.createdAt }.isNotNull
        assertThat(entity).extracting { it.createdBy }.isEqualTo("user123")
    }

    @Test
    @WithMockUser
    fun `admin-shareable-links GET - normal user yields forbidden`() {
        this.mockMvc.perform(
            get("/admin/shareable-links")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-shareable-links GET - returns empty list`() {
        this.mockMvc.perform(
            get("/admin/shareable-links")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"], username = "foo.bar")
    fun `admin-shareable-links GET - success`() {
        // given
        val car0 = testDataGenerator
            .withCar()
            .getCar()
            .value()
        val car1 = testDataGenerator
            .withCar()
            .getCar()
            .value()
        val reference0 = this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", car0.id)
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "name1" } """)
        )
            .andReturn()
            .toObject<ShareableLinkDto>()
        val reference1 = this.mockMvc.perform(
            post("/admin/cars/{carId}/shareable-links", car1.id)
                .contentType(APPLICATION_JSON)
                .content("""{ "recipientName": "name2" } """)
        )
            .andReturn()
            .toObject<ShareableLinkDto>()

        // when / then
        this.mockMvc.perform(
            get("/admin/shareable-links")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].shareableLinkReference").value(reference0.shareableLinkReference.toString()))
            .andExpect(jsonPath("$[0].carDetails.brand").value(car0.brand))
            .andExpect(jsonPath("$[0].carDetails.type").value(car0.type))
            .andExpect(jsonPath("$[0].carId").value(reference0.carId.toString()))
            .andExpect(jsonPath("$[0].recipientName").value("name1"))
            .andExpect(jsonPath("$[0].visitorCount").value(0))
            .andExpect(jsonPath("$[0].createdBy").value("foo.bar"))
            .andExpect(jsonPath("$[0].createdAt").isNotEmpty)
            .andExpect(jsonPath("$[1].shareableLinkReference").value(reference1.shareableLinkReference.toString()))
            .andExpect(jsonPath("$[1].carDetails.brand").value(car1.brand))
            .andExpect(jsonPath("$[1].carDetails.type").value(car1.type))
            .andExpect(jsonPath("$[1].carId").value(reference1.carId.toString()))
            .andExpect(jsonPath("$[1].recipientName").value("name2"))
            .andExpect(jsonPath("$[1].visitorCount").value(0))
            .andExpect(jsonPath("$[1].createdBy").value("foo.bar"))
            .andExpect(jsonPath("$[1].createdAt").isNotEmpty)
    }

    @Test
    @WithMockUser
    fun `admin-shareable-links-reference DELETE - normal user yields forbidden`() {
        this.mockMvc.perform(
            delete("/admin/shareable-links/{ref}", "some-ref")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-shareable-links-reference DELETE - unknown ref yields 404`() {
        this.mockMvc.perform(
            delete("/admin/shareable-links/{ref}", "some-ref")
        )
            .andExpect(status().isNotFound)
    }


    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin-shareable-links-reference DELETE - success`() {
        // given
        val reference0 = testDataGenerator
            .withSharedLinkReference()
            .getSharedLinkReference()
            .value()
        val reference1 = testDataGenerator
            .withSharedLinkReference()
            .getSharedLinkReference()
            .value()

        // when
        this.mockMvc.perform(
            delete("/admin/shareable-links/{ref}", reference0.shareableLinkReference)
        )
            .andExpect(status().isNoContent)

        // then
        this.mockMvc.perform(
            get("/admin/shareable-links")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].shareableLinkReference").value(reference1.shareableLinkReference.toString()))
    }
}
