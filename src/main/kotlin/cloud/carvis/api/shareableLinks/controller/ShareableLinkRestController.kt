package cloud.carvis.api.shareableLinks.controller

import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.shareableLinks.model.ShareableLinkReference
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/shareable-links")
class ShareableLinkRestController(
    private val shareableLinkService: ShareableLinkService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{shareableLinkReference}/car")
    fun fetchCarFromShareableLink(@PathVariable shareableLinkReference: ShareableLinkReference): CarDto {
        logger.info { "start fetchCarFromShareableLink(shareableLinkReference=$shareableLinkReference)" }
        return shareableLinkService.fetchCarFromShareableLinkReference(shareableLinkReference)
            .also { logger.info { "end fetchCarFromShareableLink(carId=$shareableLinkReference), return=${it}" } }
    }
}
