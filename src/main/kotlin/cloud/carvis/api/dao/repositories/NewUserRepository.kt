package cloud.carvis.api.dao.repositories

import cloud.carvis.api.model.entities.NewUserEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface NewUserRepository : CrudRepository<NewUserEntity, String>
