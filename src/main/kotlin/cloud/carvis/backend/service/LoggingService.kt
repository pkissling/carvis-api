package cloud.carvis.backend.service

import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.*

@Service
class LoggingService {

    private val logger = KotlinLogging.logger {}

    fun addTraceId(traceId: String) {
        MDC.put(MDC_TRACE_ID_KEY, traceId)
    }

    @Synchronized
    fun getTraceId(): String {
        val traceId = MDC.get(MDC_TRACE_ID_KEY)
        if (!traceId.isNullOrBlank()) {
            return traceId
        }

        val newTraceId = UUID.randomUUID().toString()
        logger.warn { "Current request does not have traceId. Generating new one: $newTraceId " }
        this.addTraceId(newTraceId)
        return newTraceId

    }

    companion object {
        const val MDC_TRACE_ID_KEY: String = "traceId"
    }
}
