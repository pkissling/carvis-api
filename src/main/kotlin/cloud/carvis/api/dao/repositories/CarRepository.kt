package cloud.carvis.api.dao.repositories

import cloud.carvis.api.model.entities.CarEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import java.util.*

@EnableScan
interface CarRepository : CrudRepository<CarEntity, UUID>
