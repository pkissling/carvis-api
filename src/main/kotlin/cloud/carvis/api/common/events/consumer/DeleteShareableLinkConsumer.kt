package cloud.carvis.api.common.events.consumer

import cloud.carvis.api.common.events.model.CarDeletedEvent
import cloud.carvis.api.common.events.model.CarvisEvent
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DeleteShareableLinkConsumer(
    private val shareableLinkService: ShareableLinkService
) : CarvisEventConsumer<CarDeletedEvent> {

    private val logger = KotlinLogging.logger {}

    override fun canConsume(event: CarvisEvent): Boolean =
        event is CarDeletedEvent

    override fun consume(event: CarDeletedEvent) {
        val entities = shareableLinkService.findByCarId(event.carId)
        if (entities.isNotEmpty()) {
            val ids = entities
                .map { it.shareableLinkReference }
                .joinToString(", ")
            logger.info { "Deleting shared link reference(s) [$ids] for carId: ${event.carId}" }
            shareableLinkService.delete(*entities.toTypedArray())
        }
    }
}

