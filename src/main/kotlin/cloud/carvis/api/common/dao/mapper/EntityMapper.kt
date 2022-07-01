package cloud.carvis.api.common.dao.mapper

import cloud.carvis.api.common.dao.model.Entity
import cloud.carvis.api.common.mapper.Mapper
import java.util.*

interface EntityMapper<HashKey, D, E : Entity<HashKey>> : Mapper<D, E> {

    fun forUpdate(id: UUID, dto: D, entity: E): E =
        this.toEntity(dto)
            .apply {
                this.hashKey = entity.hashKey
                this.createdAt = entity.createdAt
                this.createdBy = entity.createdBy
            }
}
