package cloud.carvis.api.shareableLinks.controller

import cloud.carvis.api.shareableLinks.model.CreateShareableLinkRequestDto
import cloud.carvis.api.shareableLinks.model.ShareableLinkDto
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/admin")
class AdminShareableLinkRestController(
    private val shareableLinkService: ShareableLinkService
) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/cars/{carId}/shareable-links")
    fun createShareableLink(@PathVariable carId: UUID, @Valid @RequestBody request: CreateShareableLinkRequestDto): ShareableLinkDto {
        logger.info { "start createShareableLink(carId=$carId,request=$request)" }
        return shareableLinkService.createShareableLink(carId, request)
            .also { logger.info { "end createShareableLink(carId=$carId,request=$request), return=${it}" } }
    }
}

