package cloud.carvis.api.shareableLinks.model

import java.time.Instant
import java.util.*

data class ShareableLinkDto(
    val shareableLinkReference: String? = null,
    val carId: UUID? = null,
    val carDetails: CarDetails? = null,
    val visitorCount: Long? = null,
    val recipientName: String? = null,
    val createdBy: String? = null,
    val createdAt: Instant? = null
)

data class CarDetails(
    val brand: String? = null,
    val type: String? = null
)
