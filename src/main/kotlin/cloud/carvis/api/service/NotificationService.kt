package cloud.carvis.api.service

import cloud.carvis.api.clients.Auth0RestClient
import cloud.carvis.api.model.events.UserSignupEvent
import cloud.carvis.api.properties.EmailProperties
import mu.KotlinLogging
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val emailService: EmailService,
    private val auth0RestClient: Auth0RestClient,
    private val emailProperties: EmailProperties,
) {

    private val logger = KotlinLogging.logger {}

    fun notifyUserSignup(event: UserSignupEvent) {
        val adminEmails = auth0RestClient.fetchAllAdmins()
            .map { it.email }
            .filter { it.isNotEmpty() }

        if (adminEmails.isEmpty()) {
            throw RuntimeException("No admin with mail address found in auth0!")
        }

        logger.info { "Sending email for user signup of [${event.email}] to: $adminEmails" }

        emailService.sendMail(SimpleMailMessage().apply {
            setTo(*adminEmails.toTypedArray())
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
