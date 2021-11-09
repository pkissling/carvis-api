package cloud.carvis.api.config

import cloud.carvis.api.filter.TraceIdFilter
import cloud.carvis.api.service.LoggingService
import cloud.carvis.api.service.LoggingService.Companion.MDC_TRACE_ID_KEY
import io.sentry.IHub
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.WebRequest


@Configuration
class HttpConfig {

    @Bean
    fun traceIdFilter(loggingService: LoggingService, sentryHub: IHub): TraceIdFilter =
        TraceIdFilter(loggingService, sentryHub)

    @Bean
    fun errorAttributes(loggingService: LoggingService): ErrorAttributes {
        return object : DefaultErrorAttributes() {
            override fun getErrorAttributes(
                webRequest: WebRequest?,
                options: ErrorAttributeOptions?
            ): MutableMap<String, Any> =
                super.getErrorAttributes(webRequest, options).apply {
                    put(MDC_TRACE_ID_KEY, loggingService.getTraceId())
                }
        }
    }
}
