package cloud.carvis.api.common.notifications

import cloud.carvis.api.common.events.model.UserSignupEvent
import cloud.carvis.api.common.properties.EmailProperties
import mu.KotlinLogging
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val emailService: EmailService,
    private val emailProperties: EmailProperties
) {

    private val logger = KotlinLogging.logger {}

    fun notifyUserSignup(event: UserSignupEvent) {
        val toMails = emailProperties.userSignup.toMails

        logger.info { "Sending email for user signup of [${event.email}] to: $toMails" }

        emailService.sendMail(SimpleMailMessage().apply {
            setTo(*toMails.toTypedArray())
            setFrom(emailProperties.userSignup.fromMail)
            setSubject("Neuer Nutzer")
            setText(
                """
                Hi,
                
                ein neuer Nutzer hat sich registiert:
                
                User-ID: ${event.userId}
                E-Mail: ${event.email}
                Name: ${event.name ?: "n/a"}
                
                Bitte gib dem Nutzer die entsprechende Berechtigung: ${emailProperties.userSignup.userManagementUrl}
                """.trimIndent()
            )
        })

    }
}
