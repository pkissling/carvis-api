package cloud.carvis.api.model.events

import java.util.*

data class CarvisCommand(
    val id: UUID,
    val type: CarvisCommandType,
)

enum class CarvisCommandType {
    DELETE_IMAGE
}
