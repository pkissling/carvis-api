package cloud.carvis.api.common.commands.model

import cloud.carvis.api.shareableLinks.model.ShareableLinkReference
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS
import java.util.*

@JsonTypeInfo(use = CLASS)
sealed class CarvisCommand<T>(
    open val id: T,
)

data class DeleteImageCommand(
    override val id: UUID,
) : CarvisCommand<UUID>(id)

data class AssignImageToCarCommand(
    override val id: UUID,
    val imageId: UUID
) : CarvisCommand<UUID>(id)

data class IncreaseVisitorCountCommand(
    override val id: ShareableLinkReference,
) : CarvisCommand<ShareableLinkReference>(id)
