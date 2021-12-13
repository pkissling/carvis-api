package cloud.carvis.api.filter

import cloud.carvis.api.service.LoggingService
import io.sentry.IHub
import mu.KotlinLogging
import java.util.*
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class TraceIdFilter(
    private val loggingService: LoggingService,
    private val sentryHub: IHub
) : Filter {

    private val logger = KotlinLogging.logger {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) = try {

        val httpRequest = request as HttpServletRequest
        val sentryTraceId = sentryHub.span?.spanContext?.traceId.toString()

        if (sentryTraceId.isBlank()) {
            val uuid = UUID.randomUUID().toString()
            loggingService.addTraceId(uuid)
            logger.warn { "Incoming request to [${httpRequest.method} ${httpRequest.requestURI}] did not have a Sentry traceId. Generated new traceId: $uuid" }
        } else {
            logger.trace { "Mapping existing sentry-trace from request: $sentryTraceId" }
            loggingService.addTraceId(sentryTraceId)
        }

    } catch (e: Exception) {
        logger.error(e) { "Unable to extract Sentry traceId from Sentry context" }
    } finally {
        chain.doFilter(request, response)
    }
}


