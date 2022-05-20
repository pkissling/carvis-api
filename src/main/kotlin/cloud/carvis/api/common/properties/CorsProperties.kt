package cloud.carvis.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("cors")
class CorsProperties {

    lateinit var allowedOrigins: List<String>
    lateinit var allowedMethods: List<String>
}
