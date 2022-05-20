package cloud.carvis.api.common.notifications

import cloud.carvis.api.common.properties.EmailProperties
import mu.KotlinLogging
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: MailSender,
    private val emailProperties: EmailProperties
) {

    private val logger = KotlinLogging.logger {}

    fun sendMail(mail: SimpleMailMessage) {
        if (!emailProperties.enabled) {
            logger.warn { "Not sending email, because it's disabled: $mail" }
            return
        }
        mailSender.send(mail)
    }
}
