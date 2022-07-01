package cloud.carvis.api.common.dao.model

import java.time.Instant

abstract class Entity<HashKey> {
    abstract var hashKey: HashKey?
    abstract var createdAt: Instant?
    abstract var createdBy: String?
    abstract var updatedAt: Instant?
    abstract var updatedBy: String?
    abstract fun generateHashKey(): HashKey
}
