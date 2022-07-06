package cloud.carvis.api.common.events.listener

import cloud.carvis.api.common.events.consumer.CarvisEventConsumer
import cloud.carvis.api.common.events.model.CarvisEvent
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy.NO_REDRIVE
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CarvisEventListener(
    private val eventConsumers: Set<CarvisEventConsumer<*>>
) {

    private val logger = KotlinLogging.logger {}

    @SqsListener("\${sqs.queues.carvis-event}", deletionPolicy = NO_REDRIVE)
    fun onMessage(event: CarvisEvent) {
        logger.info("Received $event")

        val errors = eventConsumers
            .filter { it.canConsume(event) }
            .ifEmpty { throw RuntimeException("No handler register for event: $event") }
            .mapNotNull { consumeEvent(it, event) }

        if (errors.isNotEmpty()) {
            throw errors.first()
        }
    }

    private fun <T : CarvisEvent> consumeEvent(consumer: CarvisEventConsumer<T>, event: CarvisEvent): Exception? = try {
        consumer.consume(event as T)
        null
    } catch (e: Exception) {
        logger.error(e) { "Error while executing consuming event [$event] with consumer: ${consumer::class.simpleName}" }
        e
    }
}
