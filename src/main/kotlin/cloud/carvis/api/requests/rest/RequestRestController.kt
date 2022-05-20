package cloud.carvis.api.requests.rest

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.requests.service.RequestService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
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

    @GetMapping("/{id}")
    fun fetchRequest(@PathVariable id: UUID): RequestDto {
        logger.info { "start fetchRequest(id=$id)" }
        return requestService.fetchRequest(id)
            .also { logger.info { "end fetchRequest(id=$id) return=${it}" } }
    }

    @PostMapping
    fun createRequest(@Valid @RequestBody request: RequestDto): RequestDto {
        logger.info { "start createRequest(request=$request)" }
        return requestService.createRequest(request)
            .also { logger.info { "end createRequest(request=$request), return=${it}" } }
    }


    @PutMapping("/{id}")
    fun updateRequest(@PathVariable id: UUID, @Valid @RequestBody request: RequestDto): RequestDto {
        logger.info { "start updateRequest(id=$id,request=$request)" }
        return requestService.updateRequest(id, request)
            .also { logger.info { "end updateRequest(id=$id,request=$request), return=${it}" } }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteRequest(@PathVariable id: UUID) {
        logger.info { "start deleteRequest(id=$id)" }
        requestService.deleteRequest(id)
        logger.info { "end deleteRequest(id=$id)" }
    }
}
