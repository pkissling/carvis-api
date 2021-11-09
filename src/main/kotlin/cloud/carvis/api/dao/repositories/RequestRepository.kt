package cloud.carvis.api.dao.repositories

import cloud.carvis.api.model.entities.RequestEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import java.util.*

@EnableScan
interface RequestRepository : CrudRepository<RequestEntity, UUID> {

    fun findAllByCreatedBy(createdBy: String): List<RequestEntity>

}
