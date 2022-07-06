package cloud.carvis.api.common.commands.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS
import java.util.*

@JsonTypeInfo(use = CLASS)
sealed class CarvisCommand

data class DeleteImageCommand(
    val imageId: UUID,
) : CarvisCommand()

data class AssignImageToCarCommand(
    val carId: UUID,
    val imageId: UUID
) : CarvisCommand()
