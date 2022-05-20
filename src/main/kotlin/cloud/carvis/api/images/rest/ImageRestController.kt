package cloud.carvis.api.images.rest

import cloud.carvis.api.images.model.ImageDto
import cloud.carvis.api.images.model.ImageHeight
import cloud.carvis.api.images.service.ImageService
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/images")
class ImageRestController(
    val imageService: ImageService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{imageId}")
    fun fetchImage(
        @PathVariable imageId: UUID,
        @RequestParam(defaultValue = "ORIGINAL") height: ImageHeight
    ): ImageDto {
        logger.info { "start fetchImage(imageId=$imageId,height=$height)" }
        return imageService.fetchImage(imageId, height)
            .also { logger.info { "end fetchImage(imageId=$imageId,height=$height), return=${it}" } }
    }

    @PostMapping
    fun generateImageUploadUrl(@RequestHeader("Content-Type") contentType: MediaType): ImageDto {
        logger.info { "start generateImageUploadUrl(contentType=$contentType)" }
        return imageService.generateImageUploadUrl(contentType)
            .also { logger.info { "end generateImageUploadUrl(contentType=$contentType), return=$it" } }
    }
}
