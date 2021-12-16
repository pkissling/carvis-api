package cloud.carvis.api.config

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.cloud.sleuth.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.WebRequest


@Configuration
class HttpConfig {

    @Bean
    fun errorAttributes(tracer: Tracer): ErrorAttributes {
        return object : DefaultErrorAttributes() {
            override fun getErrorAttributes(
                webRequest: WebRequest?,
                options: ErrorAttributeOptions?
            ): MutableMap<String, Any> =
                super.getErrorAttributes(webRequest, options).apply {
                    val traceId = tracer.currentTraceContext()?.context()?.traceId()
                    if (traceId != null) {
                        put("traceId", traceId)
                    }
                }
        }
    }
}
