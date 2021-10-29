package cloud.carvis.backend.integration

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.util.AbstractApplicationTest
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.DAYS


class ImageRestControllerTest : AbstractApplicationTest() {

    @Test
    @WithMockUser
    fun `images GET - image not found`() {
        // given
        testDataGenerator.withEmptyBucket()

        // when / then
        this.mockMvc.perform(get("/images/{id}?size={size}", "c2371741-2d29-4830-8ef1-c0d75ea9499f", "888"))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `images GET - with image`() {
        // given
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage()
            .getImage()

        // when
        val result = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.size))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.size").value(image.size))
            .andExpect(jsonPath("$.expiration", notNullValue()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()

        // then
        val img: ImageDto = toObject(result)
        assertThat(img.expiration).isBetween(`in`(6, DAYS), `in`(7, DAYS))
    }

    @Test
    @WithMockUser
    fun `images POST - create presigned url success`() {
        // when
        val start = now()
        val result = this.mockMvc.perform(
            post("/images")
                .contentType(MediaType.IMAGE_PNG)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.size").value("original"))
            .andExpect(jsonPath("$.expiration", notNullValue()))
            .andExpect(jsonPath("$.url").exists())
            .andReturn()

        // then
        val img: ImageDto = toObject(result)
        assertThat(img.expiration).isBetween(start, `in`(1, DAYS))
    }

    @Test
    @WithMockUser
    fun `images POST - without content-type`() {
        // when / then
        this.mockMvc.perform(post("/images"))
            .andExpect(status().isBadRequest)
    }

    private fun `in`(i: Long, unit: ChronoUnit) = now().plus(i, unit)

    private inline fun <reified T> toObject(result: MvcResult): T =
        objectMapper.readValue(result.response.contentAsByteArray)

}
