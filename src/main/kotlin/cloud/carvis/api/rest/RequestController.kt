package cloud.carvis.api.rest

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.service.RequestService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/requests")
class RequestController(
    val requestService: RequestService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun fetchAllRequests(): List<RequestDto> {
        logger.info { "start fetchAllRequests()" }
        return requestService.fetchAllRequests()
            .also { logger.info { "end fetchAllRequests(), return=${it}" } }
    }

    @GetMapping("/{id}")
    fun fetchRequest(@PathVariable id: UUID): RequestDto {
        logger.info { "start fetchRequest(id=$id)" }
        return requestService.fetchRequest(id)
            .also { logger.info { "end fetchRequest(id=$id) return=${it}" } }
    }
}
