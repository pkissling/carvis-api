package cloud.carvis.api.common.events.publisher

import cloud.carvis.api.common.events.model.CarDeletedEvent
import cloud.carvis.api.common.events.model.CarvisEvent
import cloud.carvis.api.common.events.model.ShareableLinkVisited
import cloud.carvis.api.common.properties.SqsQueues
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*


@Service
class CarvisEventPublisher(
    private val template: QueueMessagingTemplate,
    private val sqsQueues: SqsQueues
) {

    private val logger = KotlinLogging.logger {}

    fun carDeleted(carId: UUID, imageIds: List<UUID>) = publish(CarDeletedEvent(carId, imageIds))

    fun shareableLinkVisited(shareableLinkReference: String) = publish(ShareableLinkVisited(shareableLinkReference))

    private fun publish(event: CarvisEvent) {
        logger.info { "Publishing: $event" }
        template.convertAndSend(sqsQueues.carvisEvent, event)
    }
}
