package cloud.carvis.api.common.commands.listener

import cloud.carvis.api.common.commands.model.AssignImageToCarCommand
import cloud.carvis.api.common.commands.model.CarvisCommand
import cloud.carvis.api.common.commands.model.DeleteImageCommand
import cloud.carvis.api.images.service.ImageService
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CarvisCommandListener(
    private val imageService: ImageService,
) {

    private val logger = KotlinLogging.logger {}

    @SqsListener("\${sqs.queues.carvis-command}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    fun onMessage(command: CarvisCommand) {
        logger.info("Received $command")

        when (command) {
            is AssignImageToCarCommand -> imageService.assignCarIdToImage(command.carId, command.imageId)
            is DeleteImageCommand -> imageService.deleteImage(command.imageId)
        }
    }
}
