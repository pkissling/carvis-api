package cloud.carvis.backend.model.dtos

import java.net.URL
import java.time.Instant
import java.util.*

data class ImageDto(
    val id: UUID,
    val url: URL,
    val size: String,
    val expiration: Instant
)
