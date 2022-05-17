package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.properties.S3Buckets
import com.amazonaws.services.s3.AmazonS3
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


class CarvisCommandListenerTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var amazonS3: AmazonS3

    @Autowired
    private lateinit var s3Buckets: S3Buckets

    @Test
    fun `onMessage - DELETE_IMAGE - success`() {
        // given
        val image = testDataGenerator
            .withImage()
            .getImage()

        // when
        testDataGenerator
            .withDeleteImageCommand(image.id)

        // then
        awaitAssert {
            assertThat(amazonS3.listObjects(s3Buckets.images, "${image.id}").objectSummaries).isEmpty()
        }
    }

    @Test
    fun `onMessage - DELETE_IMAGE - processing error`() {
        // given
        testDataGenerator.withEmptyBuckets()

        // when
        testDataGenerator
            .withDeleteImageCommand(UUID.randomUUID())

        // then
        awaitAssert {
            assertThat(testDataGenerator.getCarvisCommandDlqMessages()).hasSize(1)
        }
    }
}
