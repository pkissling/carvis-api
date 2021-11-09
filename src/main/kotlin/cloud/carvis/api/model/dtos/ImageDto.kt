package cloud.carvis.api.model.dtos

import java.net.URL
import java.time.Instant
import java.util.*

data class ImageDto(
    val id: UUID,
    val url: URL,
    val size: ImageSize,
    val expiration: Instant
)
