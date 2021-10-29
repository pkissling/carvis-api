package cloud.carvis.backend.model.entities

import java.time.Instant
import java.util.*

abstract class Entity {
    abstract var id: UUID?
    abstract var ownerUsername: String? // TODO rename
    abstract var createdAt: Instant?
    abstract var lastModifiedBy: String?
    abstract var updatedAt: Instant? // TODO rename
}
