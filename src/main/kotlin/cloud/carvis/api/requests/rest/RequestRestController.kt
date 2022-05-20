package cloud.carvis.api.requests.rest

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.requests.service.RequestService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/requests")
class RequestRestController(
    val requestService: RequestService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun fetchAllRequests(): List<RequestDto> {
        logger.info { "start fetchAllRequests()" }
        return requestService.fetchAllRequests()
            .also { logger.info { "end fetchAllRequests(), return=${it}" } }
    }

    @GetMapping("/{requestId}")
    fun fetchRequest(@PathVariable requestId: UUID): RequestDto {
        logger.info { "start fetchRequest(requestId=$requestId)" }
        return requestService.fetchRequest(requestId)
            .also { logger.info { "end fetchRequest(requestId=$requestId) return=${it}" } }
    }

    @PostMapping
    fun createRequest(@Valid @RequestBody request: RequestDto): RequestDto {
        logger.info { "start createRequest(request=$request)" }
        return requestService.createRequest(request)
            .also { logger.info { "end createRequest(request=$request), return=${it}" } }
    }


    @PutMapping("/{requestId}")
    @PreAuthorize("@authorization.canModifyRequest(#requestId)")
    fun updateRequest(@PathVariable requestId: UUID, @Valid @RequestBody request: RequestDto): RequestDto {
        logger.info { "start updateRequest(requestId=$requestId,request=$request)" }
        return requestService.updateRequest(requestId, request)
            .also { logger.info { "end updateRequest(requestId=$requestId,request=$request), return=${it}" } }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{requestId}")
    @PreAuthorize("@authorization.canModifyRequest(#requestId)")
    fun deleteRequest(@PathVariable requestId: UUID) {
        logger.info { "start deleteRequest(requestId=$requestId)" }
        requestService.deleteRequest(requestId)
        logger.info { "end deleteRequest(requestId=$requestId)" }
    }
}
