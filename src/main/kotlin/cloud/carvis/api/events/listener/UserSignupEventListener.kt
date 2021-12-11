package cloud.carvis.api.events.listener

import cloud.carvis.api.model.events.UserSignupEvent
import cloud.carvis.api.service.NotificationService
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy.NO_REDRIVE
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserSignupEventListener(private val notificationService: NotificationService) {

    private val logger = KotlinLogging.logger {}

    @SqsListener("\${sqs.queues.user-signup}", deletionPolicy = NO_REDRIVE)
    fun onMessage(event: UserSignupEvent) {
        logger.info("received $event")
        notificationService.notifyUserSignup(event)
    }
}
