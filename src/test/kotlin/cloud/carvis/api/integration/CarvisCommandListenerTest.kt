package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.common.properties.S3Buckets
import cloud.carvis.api.images.model.ImageHeight.ORIGINAL
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
    fun `onMessage - DeleteImageCommand - success`() {
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
    fun `onMessage - DeleteImageCommand - processing error`() {
        // given
        testDataGenerator.withEmptyBuckets()

        // when
        testDataGenerator
            .withDeleteImageCommand(UUID.randomUUID())

        // then
        awaitAssert {
            assertThat(testDataGenerator.getCarvisCommandDlqMessageCount()).isEqualTo(1)
        }
    }

    @Test
    fun `onMessage - AssignImageToCarCommand - success`() {
        // given
        val image = testDataGenerator
            .withImage()
            .getImage()
        val car = testDataGenerator
            .withCar()
            .getCar()
            .value()

        // when
        testDataGenerator
            .withAssignImageToCarCommand(car.id!!, image.id)

        // then
        awaitAssert {
            assertThat(amazonS3.getObjectMetadata(s3Buckets.images, "${image.id}/$ORIGINAL"))
                .extracting { it.userMetadata["carId"] }.isEqualTo(car.id.toString())
        }
    }

    @Test
    fun `onMessage - AssignImageToCarCommand - processing error`() {
        // given
        testDataGenerator.withEmptyBuckets()

        // when
        testDataGenerator
            .withAssignImageToCarCommand(UUID.randomUUID(), UUID.randomUUID())

        // then
        awaitAssert {
            assertThat(testDataGenerator.getCarvisCommandDlqMessageCount()).isEqualTo(1)
        }
    }
}
