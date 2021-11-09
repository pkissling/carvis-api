package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.model.dtos.ImageDto
import cloud.carvis.api.properties.S3Properties
import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    lateinit var s3Properties: S3Properties

    @Autowired
    lateinit var amazonS3: AmazonS3

    @Test
    @WithMockUser
    fun `images GET - image not found`() {
        // given
        testDataGenerator
            .withEmptyBucket()

        // when / then
        this.mockMvc.perform(get("/images/{id}?size={size}", "c2371741-2d29-4830-8ef1-c0d75ea9499f", "500"))
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
            .andExpect(jsonPath("$.size").value(image.size.toString()))
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
            .andExpect(jsonPath("$.size").value("ORIGINAL"))
            .andExpect(jsonPath("$.expiration").exists())
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

    @Test
    @WithMockUser
    fun `images GET - resize original image`() {
        // given
        val imagesBucket = s3Properties.bucketNames["images"]
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage("mercedes.jpeg")
            .getImage()

        // when
        this.mockMvc.perform(get("/images/{id}?size={size}", image.id, 200))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.size").value("200"))
            .andExpect(jsonPath("$.expiration").exists())
            .andExpect(jsonPath("$.url").exists())

        // then
        assertThat(amazonS3.listObjects(imagesBucket).objectSummaries).hasSize(2)
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/ORIGINAL")).isTrue
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/200")).isTrue
        assertThat(amazonS3.getObject(imagesBucket, "${image.id}/200").objectMetadata.contentType).isEqualTo("image/jpeg")
    }

    @Test
    @WithMockUser
    fun `images GET - with default image size`() {
        // given
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage()
            .getImage()

        // when / then
        this.mockMvc.perform(get("/images/{id}", image.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.size").value("ORIGINAL"))
            .andExpect(jsonPath("$.expiration", notNullValue()))
            .andExpect(jsonPath("$.url", notNullValue()))
    }

    @Test
    @WithMockUser
    fun `images GET - with invalid image size`() {
        // given
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage()
            .getImage()

        // when / then
        this.mockMvc.perform(get("/images/{id}?size={size}", image.id, "199"))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `images GET - cache image url`() {
        // given
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage()
            .getImage()

        // when
        val result0 = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.size))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()
        val result1 = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.size))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()

        // then
        val imageDto0: ImageDto = toObject(result0)
        val imageDto1: ImageDto = toObject(result1)
        assertThat(imageDto0.url).isEqualTo(imageDto1.url)
        assertThat(imageDto0.expiration).isEqualTo(imageDto1.expiration)
    }

    private fun `in`(i: Long, unit: ChronoUnit) = now().plus(i, unit)

    private inline fun <reified T> toObject(result: MvcResult): T =
        objectMapper.readValue(result.response.contentAsByteArray)

}
