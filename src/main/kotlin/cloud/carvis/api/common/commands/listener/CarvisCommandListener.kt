package cloud.carvis.api.common.commands.listener

import cloud.carvis.api.common.commands.model.AssignImageToCarCommand
import cloud.carvis.api.common.commands.model.CarvisCommand
import cloud.carvis.api.common.commands.model.DeleteImageCommand
import cloud.carvis.api.common.commands.model.IncreaseVisitorCountCommand
import cloud.carvis.api.images.service.ImageService
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CarvisCommandListener(
    private val imageService: ImageService,
    private val shareableLinkService: ShareableLinkService
) {

    private val logger = KotlinLogging.logger {}

    @SqsListener("\${sqs.queues.carvis-command}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    fun <T> onMessage(command: CarvisCommand<T>) {
        logger.info("Received $command")

        when (command) {
            is AssignImageToCarCommand -> imageService.assignCarIdToImage(command.id, command.imageId)
            is DeleteImageCommand -> imageService.deleteImage(command.id)
            is IncreaseVisitorCountCommand -> shareableLinkService.increaseVisitorCount(command.id)
        }
    }
}
