package cloud.carvis.api.sentry

import io.sentry.SamplingContext
import io.sentry.SentryOptions.TracesSamplerCallback
import io.sentry.spring.boot.SentryProperties
import mu.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest


@EnableConfigurationProperties(SentryProperties::class)
@Component
class SentryTracesSamplerCallback(private val sentryProperties: SentryProperties) : TracesSamplerCallback {

    private val logger = KotlinLogging.logger {}

    override fun sample(context: SamplingContext): Double {
        val request = context.customSamplingContext!!["request"] as HttpServletRequest?

        return when (request?.requestURI) {
            "/actuator" -> 0.0
            else -> fromProperties()
        }

    }

    private fun fromProperties(): Double {
        return if (sentryProperties.tracesSampleRate != null) {
            sentryProperties.tracesSampleRate!!
        } else {
            logger.warn { "Could not determine tracesSampleRate. Returning 0" }
            0.0
        }
    }
}



