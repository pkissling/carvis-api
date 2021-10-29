package cloud.carvis.backend.rest

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.service.ImageService
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
    fun fetchImage(@PathVariable id: UUID, @RequestParam(defaultValue = "original") size: String): ImageDto {
        logger.info { "start fetchImage(id=$id,size=$size)" }
        return imageService.fetchImage(id, size)
            .also { logger.info { "end fetchImage(id=$id,size=$size), return=${it}" } }
    }

    @PostMapping
    fun createPresignedUrl(@RequestHeader("Content-Type") contentType: MediaType): ImageDto {
        logger.info { "start createPresignedUrl(contentType=$contentType)" }
        return imageService.createPresignedUrl(contentType)
            .also { logger.info { "end createPresignedUrl(contentType=$contentType), return=$it" } }
    }
}
