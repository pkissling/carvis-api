package cloud.carvis.api.images.model

import java.net.URL
import java.time.Instant
import java.util.*

data class ImageDto(
    val id: UUID,
    val url: URL,
    val height: ImageHeight,
    val expiresAt: Instant
)
