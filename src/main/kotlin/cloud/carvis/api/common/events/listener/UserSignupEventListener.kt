package cloud.carvis.api.common.events.listener

import cloud.carvis.api.common.events.model.UserSignupEvent
import cloud.carvis.api.common.notifications.NotificationService
import cloud.carvis.api.users.service.UserService
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy.NO_REDRIVE
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserSignupEventListener(
    private val notificationService: NotificationService,
    private val userService: UserService
) {

    private val logger = KotlinLogging.logger {}
    private val userSignupFunctions: List<(e: UserSignupEvent) -> Unit> = listOf(
        { event -> notificationService.notifyUserSignup(event) },
        { event -> userService.persistNewUserSignup(event) }
    )

    @SqsListener("\${sqs.queues.user-signup}", deletionPolicy = NO_REDRIVE)
    fun onMessage(event: UserSignupEvent) {
        logger.info("Received $event")

        val errors = userSignupFunctions
            .mapNotNull { consumeEvent(event, it) }

        if (errors.isNotEmpty()) {
            throw errors.first()
        }
    }

    private fun consumeEvent(event: UserSignupEvent, fn: (event: UserSignupEvent) -> Unit): Exception? = try {
        fn.invoke(event)
        null
    } catch (e: Exception) {
        logger.error(e) { "Error while executing function after receiving UserSignupEvent: $event" }
        e
    }
}
