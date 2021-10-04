package cloud.carvis.backend.repositories

import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository


@EnableScan
interface CarRepository : CrudRepository<CarEntity, String> {
    fun findById(id: String?): CarEntity?
}