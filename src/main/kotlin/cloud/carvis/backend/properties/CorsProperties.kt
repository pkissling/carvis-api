package cloud.carvis.backend.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("cors")
class CorsProperties {

    lateinit var allowedMethods: List<HttpMethod>
    lateinit var allowedOrigins: List<String>
}