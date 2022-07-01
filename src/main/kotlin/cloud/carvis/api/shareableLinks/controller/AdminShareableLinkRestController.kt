package cloud.carvis.api.shareableLinks.controller

import cloud.carvis.api.shareableLinks.model.CreateShareableLinkRequestDto
import cloud.carvis.api.shareableLinks.model.ShareableLinkDto
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import mu.KotlinLogging
import org.springframework.http.HttpStatus.NO_CONTENT
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
    fun createShareableLink(
        @PathVariable carId: UUID,
        @Valid @RequestBody request: CreateShareableLinkRequestDto
    ): ShareableLinkDto {
        logger.info { "start createShareableLink(carId=$carId,request=$request)" }
        return shareableLinkService.createShareableLink(carId, request)
            .also { logger.info { "end createShareableLink(carId=$carId,request=$request), return=${it}" } }
    }

    @GetMapping("/shareable-links")
    fun fetchShareableLinks(): List<ShareableLinkDto> {
        logger.info { "start fetchShareableLinks()" }
        return shareableLinkService.fetchShareableLinks()
            .also { logger.info { "end fetchShareableLinks(), return=${it}" } }
    }

    @DeleteMapping("/shareable-links/{shareableLinkReference}")
    @ResponseStatus(NO_CONTENT)
    fun deleteShareableLink(@PathVariable shareableLinkReference: String) {
        logger.info { "start deleteShareableLink(shareableLinkReference=$shareableLinkReference)" }
        shareableLinkService.deleteShareableLink(shareableLinkReference)
            .also { logger.info { "end deleteShareableLink(shareableLinkReference=$shareableLinkReference)" } }
    }
}

