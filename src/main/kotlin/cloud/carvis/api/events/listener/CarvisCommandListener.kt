package cloud.carvis.api.events.listener

import cloud.carvis.api.model.events.CarvisCommand
import cloud.carvis.api.model.events.CarvisCommandType
import cloud.carvis.api.service.ImageService
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy.NO_REDRIVE
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CarvisCommandListener(private val imageService: ImageService) {

    private val logger = KotlinLogging.logger {}
    private val commandProcessors: Map<CarvisCommandType, List<Pair<String, (e: CarvisCommand) -> Unit>>> = mapOf(
        CarvisCommandType.DELETE_IMAGE to listOf(
            "imageService.deleteImage(command.id)" to { command -> imageService.deleteImage(command.id) },
        )
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
            .mapNotNull { consumeEvent(command, it.first, it.second) }

        if (errors.isNotEmpty()) {
            throw errors.first()
        }
    }

    private fun consumeEvent(command: CarvisCommand, fnDescription: String, fn: (event: CarvisCommand) -> Unit): Exception? = try {
        fn.invoke(command)
        null
    } catch (e: Exception) {
        logger.error(e) { "Error while executing function after receiving CarvisCommand: $fnDescription" }
        e
    }
}
