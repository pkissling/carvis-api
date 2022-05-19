package cloud.carvis.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("auth")
class AuthProperties {

    lateinit var audiences: Map<AudienceType, String>

}

enum class AudienceType {
    USER, SYSTEM
}
