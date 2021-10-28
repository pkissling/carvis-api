package cloud.carvis.backend.integration

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.util.AbstractApplicationTest
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.temporal.ChronoUnit


class ImagesRestControllerTest : AbstractApplicationTest() {

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

        // when / then
        val result = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.size))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.size").value(image.size))
            .andExpect(jsonPath("$.expiration", notNullValue()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()

        val img: ImageDto = toObject(result)
        assertThat(img.expiration).isBetween(days(6), days(7))
    }

    private fun days(i: Long) = Instant.now().plus(i, ChronoUnit.DAYS)

    private inline fun <reified T> toObject(result: MvcResult): T =
        objectMapper.readValue(result.response.contentAsByteArray)

}
