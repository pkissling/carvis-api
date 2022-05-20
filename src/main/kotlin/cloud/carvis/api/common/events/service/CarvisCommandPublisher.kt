package cloud.carvis.api.common.events.service

import cloud.carvis.api.common.properties.SqsQueues
import cloud.carvis.api.model.events.CarvisCommand
import cloud.carvis.api.model.events.CarvisCommandType.ASSIGN_IMAGE_TO_CAR
import cloud.carvis.api.model.events.CarvisCommandType.DELETE_IMAGE
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

    fun assignImagesToCar(carId: UUID, imageIds: List<UUID>) = imageIds.forEach { assignImageToCar(carId, it) }

    fun assignImageToCar(carId: UUID, imageId: UUID) = this.publish(CarvisCommand(carId, ASSIGN_IMAGE_TO_CAR, imageId))

    private fun publish(command: CarvisCommand) {
        logger.info { "Publishing: $command" }
        template.convertAndSend(sqsQueues.carvisCommand, command)
    }
}
