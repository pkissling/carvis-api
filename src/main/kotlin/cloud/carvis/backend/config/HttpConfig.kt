package cloud.carvis.backend.config

import cloud.carvis.backend.filter.TraceIdFilter
import cloud.carvis.backend.service.LoggingService
import cloud.carvis.backend.service.LoggingService.Companion.MDC_TRACE_ID_KEY
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.WebRequest


@Configuration
class HttpConfig {

    @Bean
    fun traceIdFilter(loggingService: LoggingService): TraceIdFilter =
        TraceIdFilter(loggingService)

    @Bean
    fun errorAttributes(loggingService: LoggingService): ErrorAttributes {
        return object : DefaultErrorAttributes() {
            override fun getErrorAttributes(webRequest: WebRequest?, options: ErrorAttributeOptions?): MutableMap<String, Any> =
                super.getErrorAttributes(webRequest, options).apply {
                    put(MDC_TRACE_ID_KEY, loggingService.getTraceId())
                }
        }
    }
}
