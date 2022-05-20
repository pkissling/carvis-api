package cloud.carvis.api.requests.dao

import cloud.carvis.api.requests.model.entities.RequestEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import java.util.*

@EnableScan
interface RequestRepository : CrudRepository<RequestEntity, UUID>
