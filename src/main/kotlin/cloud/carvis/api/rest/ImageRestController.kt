package cloud.carvis.api.rest

import cloud.carvis.api.model.dtos.ImageDto
import cloud.carvis.api.model.dtos.ImageSize
import cloud.carvis.api.service.ImageService
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

    @GetMapping("/{id}")
    fun fetchImage(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "ORIGINAL") size: ImageSize
    ): ImageDto {
        logger.info { "start fetchImage(id=$id,size=$size)" }
        return imageService.fetchImage(id, size)
            .also { logger.info { "end fetchImage(id=$id,size=$size), return=${it}" } }
    }

    @PostMapping
    fun generateImageUploadUrl(@RequestHeader("Content-Type") contentType: MediaType): ImageDto {
        logger.info { "start generateImageUploadUrl(contentType=$contentType)" }
        return imageService.generateImageUploadUrl(contentType)
            .also { logger.info { "end generateImageUploadUrl(contentType=$contentType), return=$it" } }
    }
}
