package cloud.carvis.backend.integration

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.util.AbstractApplicationTest
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.number.OrderingComparison.greaterThan
import org.hamcrest.number.OrderingComparison.lessThan
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
        assertThat(img.expiration, isInRoughlyDays(7))
    }

    private fun isInRoughlyDays(i: Long): Matcher<Instant> {
        val lowerBound = Instant.now().plus((i - 1), ChronoUnit.DAYS)
        val upperBound = Instant.now().plus(i, ChronoUnit.DAYS)
        return allOf(greaterThan(lowerBound), lessThan(upperBound))
    }

    private inline fun <reified T> toObject(result: MvcResult): T =
        objectMapper.readValue(result.response.contentAsByteArray)

}
