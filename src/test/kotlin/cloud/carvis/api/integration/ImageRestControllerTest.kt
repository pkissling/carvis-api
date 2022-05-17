package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.model.dtos.ImageDto
import cloud.carvis.api.properties.S3Buckets
import com.amazonaws.services.s3.AmazonS3
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import javax.imageio.ImageIO


class ImageRestControllerTest : AbstractApplicationTest() {

    @Autowired
    lateinit var s3Properties: S3Buckets

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
        val result = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.height))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.height").value(image.height.toString()))
            .andExpect(jsonPath("$.expiresAt", notNullValue()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()

        // then
        val img: ImageDto = toObject(result)
        assertThat(img.expiresAt).isBetween(`in`(11, HOURS), `in`(13, HOURS))
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
            .andExpect(jsonPath("$.height").value("ORIGINAL"))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.url").exists())
            .andReturn()

        // then
        val img: ImageDto = toObject(result)
        assertThat(img.expiresAt).isBetween(start, `in`(1, DAYS))
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
        val imagesBucket = s3Properties.images
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage("mercedes.jpeg")
            .getImage()

        // when
        this.mockMvc.perform(get("/images/{id}?height={height}", image.id, 200))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.height").value(200))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.url").exists())

        // then
        assertThat(amazonS3.listObjects(imagesBucket).objectSummaries).hasSize(2)
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/ORIGINAL")).isTrue
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/200")).isTrue
        assertThat(amazonS3.getObject(imagesBucket, "${image.id}/200")).extracting { it.objectMetadata.contentType }.isEqualTo("image/jpeg")
    }

    @Test
    @WithMockUser
    fun `images GET - retain portrait orientation`() {
        // given
        val imagesBucket = s3Properties.images
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage("portrait.jpg")
            .getImage()

        // when
        this.mockMvc.perform(get("/images/{id}?height={height}", image.id, 200))
            .andExpect(status().isOk)

        // then
        assertThat(amazonS3.listObjects(imagesBucket).objectSummaries).hasSize(2)
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/ORIGINAL")).isTrue
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/200")).isTrue
        val resizedImage = amazonS3.getObject(imagesBucket, "${image.id}/200")
            .let { ImageIO.read(it.objectContent) }
        assertThat(resizedImage.height).isEqualTo(200)
        assertThat(resizedImage.width).isEqualTo(133)
    }

    @Test
    @WithMockUser
    fun `images GET - retain landscape orientation`() {
        // given
        val imagesBucket = s3Properties.images
        val image = testDataGenerator
            .withEmptyBucket()
            .withImage("mercedes.jpeg")
            .getImage()

        // when
        this.mockMvc.perform(get("/images/{id}?height={height}", image.id, 200))
            .andExpect(status().isOk)

        // then
        assertThat(amazonS3.listObjects(imagesBucket).objectSummaries).hasSize(2)
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/ORIGINAL")).isTrue
        assertThat(amazonS3.doesObjectExist(imagesBucket, "${image.id}/200")).isTrue
        val resizedImage = amazonS3.getObject(imagesBucket, "${image.id}/200")
            .let { ImageIO.read(it.objectContent) }
        assertThat(resizedImage.height).isEqualTo(85)
        assertThat(resizedImage.width).isEqualTo(200)
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
        val response = this.mockMvc.perform(get("/images/{id}", image.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.height").value("ORIGINAL"))
            .andExpect(jsonPath("$.expiresAt", notNullValue()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn().response

        response.contentAsString
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
        this.mockMvc.perform(get("/images/{id}?height={height}", image.id, "199"))
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
        val result0 = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.height))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()
        val result1 = this.mockMvc.perform(get("/images/{id}?size={size}", image.id, image.height))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id.toString()))
            .andExpect(jsonPath("$.url", notNullValue()))
            .andReturn()

        // then
        val imageDto0: ImageDto = toObject(result0)
        val imageDto1: ImageDto = toObject(result1)
        assertThat(imageDto0.url).isEqualTo(imageDto1.url)
        assertThat(imageDto0.expiresAt).isEqualTo(imageDto1.expiresAt)
    }

    private fun `in`(i: Long, unit: ChronoUnit) = now().plus(i, unit)

}
