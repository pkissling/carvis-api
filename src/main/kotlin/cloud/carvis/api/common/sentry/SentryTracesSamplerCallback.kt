package cloud.carvis.api.common.sentry

import io.sentry.SamplingContext
import io.sentry.SentryOptions.TracesSamplerCallback
import io.sentry.spring.boot.SentryProperties
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest


@EnableConfigurationProperties(SentryProperties::class)
@Component
class SentryTracesSamplerCallback(
    private val sentryProperties: SentryProperties,
    private val webEndpointProperties: WebEndpointProperties
) : TracesSamplerCallback {

    override fun sample(context: SamplingContext): Double? {
        val request = context.customSamplingContext!!["request"] as HttpServletRequest?

        return when (isActuatorCall(request)) {
            true -> 0.0
            else -> sentryProperties.tracesSampleRate
        }
    }

    private fun isActuatorCall(request: HttpServletRequest?) =
        request?.requestURI?.startsWith(webEndpointProperties.basePath)
}
