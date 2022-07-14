package cloud.carvis.api.shareableLinks.controller

import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.images.model.ImageDto
import cloud.carvis.api.images.model.ImageHeight
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/shareable-links")
class ShareableLinkRestController(
    private val shareableLinkService: ShareableLinkService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{shareableLinkReference}/car")
    fun fetchCarFromShareableLink(@PathVariable shareableLinkReference: String): CarDto {
        logger.info { "start fetchCarFromShareableLink(shareableLinkReference=$shareableLinkReference)" }
        return shareableLinkService.fetchCarFromShareableLinkReference(shareableLinkReference)
            .also { logger.info { "end fetchCarFromShareableLink(carId=$shareableLinkReference), return=${it}" } }
    }

    @GetMapping("/{shareableLinkReference}/images/{imageId}")
    fun fetchImage(
        @PathVariable shareableLinkReference: String,
        @PathVariable imageId: UUID,
        @RequestParam(defaultValue = "ORIGINAL") height: ImageHeight
    ): ImageDto {
        logger.info { "start fetchImage(shareableLinkReference=$shareableLinkReference,imageId=$imageId,height=$height)" }
        return shareableLinkService.fetchImage(shareableLinkReference, imageId, height)
            .also { logger.info { "end fetchImage(shareableLinkReference=$shareableLinkReference,imageId=$imageId,height=$height), return=${it}" } }
    }
}
