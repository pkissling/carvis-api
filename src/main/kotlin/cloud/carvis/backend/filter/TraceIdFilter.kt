package cloud.carvis.backend.filter

import cloud.carvis.backend.service.LoggingService
import mu.KotlinLogging
import java.util.*
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

private val logger = KotlinLogging.logger {}

class TraceIdFilter(private val loggingService: LoggingService) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) = try {

        val httpRequest = request as HttpServletRequest
        val sentryTrace = httpRequest.getHeader(SENTRY_TRACE_KEY)

        if (sentryTrace.isNullOrBlank()) {
            val uuid = UUID.randomUUID().toString()
            loggingService.addTraceId(uuid)
            logger.info { "Incoming request to [${httpRequest.method} ${httpRequest.requestURI}] did not have a sentry-trace. Generated new trace: $uuid" }
        } else {
            logger.info { "Mapping existing sentry-trace from request: $sentryTrace" }
            loggingService.addTraceId(sentryTrace)
        }

    } catch (e: Exception) {
        logger.error(e) { "Unable to extract sentry-trace header from request" }
    } finally {
        chain.doFilter(request, response)
    }

    companion object {
        const val SENTRY_TRACE_KEY: String = "sentry-trace"
    }
}

