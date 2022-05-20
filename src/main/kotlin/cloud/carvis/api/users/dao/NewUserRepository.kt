package cloud.carvis.api.users.dao

import cloud.carvis.api.users.model.NewUserEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface NewUserRepository : CrudRepository<NewUserEntity, String>
