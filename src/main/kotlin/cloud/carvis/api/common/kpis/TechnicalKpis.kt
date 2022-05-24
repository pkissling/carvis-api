package cloud.carvis.api.common.kpis

import cloud.carvis.api.images.service.ImageService
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class TechnicalKpis {

    @Configuration
    class Queues(
        private val amazonSqs: AmazonSQS,
        @Value("\${sqs.queues.prefix}") private val sqsPrefix: String,
        private val meterRegistry: MeterRegistry
    ) {

        val logger = KotlinLogging.logger {}

        companion object {
            const val QUEUE_MESSAGES_COUNT = "queue_messages_count"
            const val QUEUE_NAME = "queue_name"
        }

        @PostConstruct
        fun postConstruct() {
            queueMessageCounter()
        }

        private fun queueMessageCounter() {
            amazonSqs.listQueues()
                .queueUrls
                .associateWith { { getQueueMessageCount(it) } }
                .mapKeys { it.key.substringAfterLast("/") }
                .filterKeys { it.startsWith(this.sqsPrefix) }
                .forEach { (queueName, supplier) -> registerQueue(queueName, supplier) }
        }

        private fun registerQueue(queueName: String, supplier: (() -> Number)) {
            logger.info { "Registering Technical Queue KPI: $queueName" }
            Gauge.builder(QUEUE_MESSAGES_COUNT, supplier)
                .tag(QUEUE_NAME, queueName)
                .register(meterRegistry)
        }

        private fun getQueueMessageCount(queueUrl: String): Int {
            return amazonSqs.getQueueAttributes(
                GetQueueAttributesRequest()
                    .withQueueUrl(queueUrl)
                    .withAttributeNames("ApproximateNumberOfMessages")
            ).attributes["ApproximateNumberOfMessages"]?.toInt() ?: 0
        }
    }

    @Configuration
    class Images(
        private val imageService: ImageService,
        private val meterRegistry: MeterRegistry
    ) {

        val logger = KotlinLogging.logger {}

        companion object {
            const val IMAGES_COUNT = "images_count"
            const val IMAGE_COUNT_TYPE = "type"
        }

        @PostConstruct
        fun postConstruct() {
            deleteImagesCount()
        }

        private fun deleteImagesCount() {
            registerKpi("deleted") { imageService.deleteImagesCount() }
        }

        private fun registerKpi(type: String, supplier: () -> Number) {
            logger.info { "Registering Technical S3 KPI : $type" }
            Gauge.builder(IMAGES_COUNT, supplier)
                .tag(IMAGE_COUNT_TYPE, type)
                .register(meterRegistry)
        }
    }
}
