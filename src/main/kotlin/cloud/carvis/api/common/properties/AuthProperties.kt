package cloud.carvis.api.common.properties

import cloud.carvis.api.common.auth.model.AudienceType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("auth")
class AuthProperties {

    lateinit var audiences: Map<AudienceType, String>

}
