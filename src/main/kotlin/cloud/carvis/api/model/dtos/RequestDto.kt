package cloud.carvis.api.model.dtos

import java.time.Instant
import java.util.*

data class RequestDto(
    var id: UUID? = null,
    var createdAt: Instant? = null,
    var createdBy: String? = null,
    var hasHiddenFields: Boolean? = null,
    var updatedAt: Instant? = null,
    var updatedBy: String? = null,
)
