package cloud.carvis.api.common.commands.publisher

import cloud.carvis.api.common.commands.model.AssignImageToCarCommand
import cloud.carvis.api.common.commands.model.CarvisCommand
import cloud.carvis.api.common.commands.model.DeleteImageCommand
import cloud.carvis.api.common.commands.model.IncreaseVisitorCountCommand
import cloud.carvis.api.common.properties.SqsQueues
import cloud.carvis.api.shareableLinks.model.ShareableLinkReference
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

    fun deleteImage(imageId: UUID) = this.publish(DeleteImageCommand(imageId))

    fun assignImagesToCar(carId: UUID, imageIds: List<UUID>) = imageIds.forEach { assignImageToCar(carId, it) }

    fun assignImageToCar(carId: UUID, imageId: UUID) = this.publish(AssignImageToCarCommand(carId, imageId))

    fun increaseVisitorCounter(reference: ShareableLinkReference) = this.publish(IncreaseVisitorCountCommand(reference))

    private fun <T> publish(command: CarvisCommand<T>) {
        logger.info { "Publishing: $command" }
        template.convertAndSend(sqsQueues.carvisCommand, command)
    }
}
