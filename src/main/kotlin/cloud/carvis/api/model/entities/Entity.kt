package cloud.carvis.api.model.entities

import java.time.Instant
import java.util.*

abstract class Entity {
    abstract var id: UUID?
    abstract var createdAt: Instant?
    abstract var createdBy: String?
    abstract var updatedAt: Instant?
    abstract var updatedBy: String?
}
