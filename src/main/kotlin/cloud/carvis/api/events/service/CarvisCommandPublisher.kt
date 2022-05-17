package cloud.carvis.api.events.service

import cloud.carvis.api.model.events.CarvisCommand
import cloud.carvis.api.model.events.CarvisCommandType.DELETE_IMAGE
import cloud.carvis.api.properties.SqsQueues
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class CarvisCommandPublisher(
    private val template: QueueMessagingTemplate,
    private val sqsQueues: SqsQueues
) {

    private val logger = KotlinLogging.logger {}

    fun deleteImages(imageIds: List<UUID>) = imageIds.forEach { deleteImage(it) }

    fun deleteImage(imageId: UUID) = this.publish(CarvisCommand(imageId, DELETE_IMAGE))

    private fun publish(command: CarvisCommand) {
        logger.info { "Publishing: $command" }
        template.convertAndSend(sqsQueues.carvisCommand, command)
    }
}
