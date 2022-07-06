package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.common.properties.S3Buckets
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import com.amazonaws.services.s3.AmazonS3
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


class CarvisEventListenerTest : AbstractApplicationTest() {

    @Autowired
    lateinit var shareableLinkRepository: ShareableLinkRepository

    @Autowired
    lateinit var amazonS3: AmazonS3

    @Autowired
    private lateinit var s3Properties: S3Buckets

    @Test
    fun `onMessage - CarDeletedEvent - success`() {
        // given
        val image = testDataGenerator
            .withImage()
            .getImage()
        val car = testDataGenerator
            .withCar(imageIds = listOf(image.id))
            .getCar()
            .value()
        testDataGenerator
            .withShareableLink(car.id!!)
            .withShareableLink(car.id!!)

        // when
        testDataGenerator.withCarDeletedEvent(car.id!!, listOf(image.id))

        // then
        awaitAssert {
            assertThat(shareableLinkRepository.count()).isEqualTo(0)
            assertThat(amazonS3.doesObjectExist(s3Properties.images, "${image.id}/${image.height}")).isFalse
            assertThat(amazonS3.doesObjectExist(s3Properties.images, "deleted/${image.id}/${image.height}")).isTrue
            assertThat(testDataGenerator.getCarvisEventMessageCount()).isEqualTo(0)
        }
    }

    @Test
    fun `onMessage - CarDeletedEvent - on processing error send to dql`() {
        // given
        val car = testDataGenerator
            .withCar()
            .getCar()
            .value()
        testDataGenerator
            .withShareableLink(car.id!!)

        // when
        testDataGenerator
            .withCarDeletedEvent(car.id!!, listOf(UUID.randomUUID()))

        // then
        awaitAssert {
            assertThat(shareableLinkRepository.count()).isEqualTo(0)
            assertThat(testDataGenerator.getCarvisEventMessageCount()).isEqualTo(0)
            assertThat(testDataGenerator.getCarvisEventDlqMessageCount()).isEqualTo(1)
        }
    }
}
