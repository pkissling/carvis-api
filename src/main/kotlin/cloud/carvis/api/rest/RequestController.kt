package cloud.carvis.api.rest

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.service.RequestService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/requests")
class RequestController(
    val requestService: RequestService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun fetchRequests(): List<RequestDto> {
        logger.info { "start fetchRequests()" }
        return requestService.fetchRequests()
            .also { logger.info { "end fetchRequests(), return=${it}" } }
    }
}
