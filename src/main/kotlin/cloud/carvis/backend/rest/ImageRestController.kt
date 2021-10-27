package cloud.carvis.backend.rest

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.service.ImageService
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*


@RestController
@RequestMapping("/images")
class ImageRestController(
    val imageService: ImageService
) {

    @GetMapping("/{id}")
    fun image(@PathVariable id: UUID, @RequestParam(defaultValue = "original") size: String): ImageDto =
        imageService.fetchImage(id, size)
            ?: throw ResponseStatusException(NOT_FOUND, "image not found")
}
