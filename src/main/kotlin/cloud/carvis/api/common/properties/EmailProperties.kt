package cloud.carvis.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import kotlin.properties.Delegates

@Validated
@Component
@ConfigurationProperties("email")
class EmailProperties {

    var enabled by Delegates.notNull<Boolean>()

    @NotNull
    var userSignup: EmailUserSignupProperties = EmailUserSignupProperties()

    class EmailUserSignupProperties {

        @NotEmpty
        lateinit var toMails: List<String>

        @NotNull
        lateinit var fromMail: String

        @NotNull
        lateinit var userManagementUrl: String

    }
}


