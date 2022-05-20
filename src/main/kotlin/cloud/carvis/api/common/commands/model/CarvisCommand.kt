package cloud.carvis.api.model.events

import java.util.*

data class CarvisCommand(
    val id: UUID,
    val type: CarvisCommandType,
    val additionalData: Any? = null
)

enum class CarvisCommandType {
    DELETE_IMAGE,
    ASSIGN_IMAGE_TO_CAR
}
