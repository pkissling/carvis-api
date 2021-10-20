package cloud.carvis.backend.dao.repositories

import cloud.carvis.backend.model.entities.CarEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import java.util.*

@EnableScan
interface CarRepository : CrudRepository<CarEntity, UUID> {
    fun findById(id: UUID?): CarEntity?
}