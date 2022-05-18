package cloud.carvis.api.events.listener

import cloud.carvis.api.model.events.CarvisCommand
import cloud.carvis.api.model.events.CarvisCommandType
import cloud.carvis.api.model.events.CarvisCommandType.ASSIGN_IMAGE_TO_CAR
import cloud.carvis.api.model.events.CarvisCommandType.DELETE_IMAGE
import cloud.carvis.api.service.ImageService
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy.NO_REDRIVE
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CarvisCommandListener(private val imageService: ImageService) {

    private val logger = KotlinLogging.logger {}
    private val commandProcessors: Map<CarvisCommandType, List<(e: CarvisCommand) -> Unit>> = mapOf(
        DELETE_IMAGE to listOf { command -> imageService.deleteImage(command.id) },
        ASSIGN_IMAGE_TO_CAR to listOf { command -> imageService.assignCarIdToImage(command.id, command.additionalData as String) },
    )

    @SqsListener("\${sqs.queues.carvis-command}", deletionPolicy = NO_REDRIVE)
    fun onMessage(command: CarvisCommand) {
        logger.info("Received $command")

        val processors = commandProcessors[command.type]
        if (processors == null) {
            logger.warn { "Did not find processor for command: $command.type" }
            return
        }
        val errors = processors
            .mapNotNull { consumeEvent(command, it) }

        if (errors.isNotEmpty()) {
            throw errors.first()
        }
    }

    private fun consumeEvent(command: CarvisCommand, fn: (event: CarvisCommand) -> Unit): Exception? = try {
        fn.invoke(command)
        null
    } catch (e: Exception) {
        logger.error(e) { "Error while executing function after receiving CarvisCommand: $command" }
        e
    }
}
