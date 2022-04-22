package cloud.carvis.api.mapper

import cloud.carvis.api.model.entities.Entity
import java.util.*

interface EntityMapper<D, E : Entity> : Mapper<D, E> {

    fun forUpdate(id: UUID, dto: D, entity: E): E =
        this.toEntity(dto)
            .apply {
                this.id = entity.id
                this.createdAt = entity.createdAt
                this.createdBy = entity.createdBy
            }
}
