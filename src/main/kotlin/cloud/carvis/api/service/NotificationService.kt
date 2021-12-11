package cloud.carvis.api.service

import cloud.carvis.api.model.events.UserSignupEvent
import cloud.carvis.api.properties.EmailProperties
import mu.KotlinLogging
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val emailService: EmailService,
    private val userService: UserService,
    private val emailProperties: EmailProperties
) {

    private val logger = KotlinLogging.logger {}

    fun notifyUserSignup(event: UserSignupEvent) {
        val adminEmails = userService.fetchAllAdminEmails()

        logger.info { "Sending email for user signup of [${event.name}] to: $adminEmails" }

        emailService.sendMail(SimpleMailMessage().apply {
            setTo(*adminEmails.toTypedArray())
            setFrom(emailProperties.userSignup.fromMail)
            setSubject("hi")
            setText("text")
        })

    }

}
