package cloud.carvis.api.common.events.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS
import java.util.*

@JsonTypeInfo(use = CLASS)
sealed class CarvisEvent

data class CarDeletedEvent(
    val carId: UUID,
    val imageIds: List<UUID>
) : CarvisEvent()

data class ShareableLinkVisited(
    val shareableLinkReference: String,
) : CarvisEvent()
