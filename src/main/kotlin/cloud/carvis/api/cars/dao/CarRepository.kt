package cloud.carvis.api.cars.dao

import cloud.carvis.api.cars.model.CarEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import java.util.*

@EnableScan
interface CarRepository : CrudRepository<CarEntity, UUID>
