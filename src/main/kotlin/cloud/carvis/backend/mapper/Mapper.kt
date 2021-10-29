package cloud.carvis.backend.mapper

import cloud.carvis.backend.model.entities.Entity
import java.util.*

interface Mapper<D, E : Entity> {

    fun toDto(entity: E): D
    fun toEntity(dto: D): E

    fun forUpdate(id: UUID, dto: D, entity: E): E {
        return this.toEntity(dto)
            .apply {
                this.id = entity.id
                this.ownerUsername = entity.ownerUsername
                this.createdAt = entity.createdAt
            }
    }
}
