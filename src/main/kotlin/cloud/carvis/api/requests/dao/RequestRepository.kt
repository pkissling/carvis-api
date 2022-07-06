package cloud.carvis.api.requests.dao

import cloud.carvis.api.common.dao.BaseRepository
import cloud.carvis.api.common.dao.DynamoDbDao
import cloud.carvis.api.requests.model.entities.RequestEntity
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class RequestRepository(
    private val dynamoDbDao: DynamoDbDao<RequestEntity, UUID>
) : BaseRepository<RequestEntity, UUID> {

    override fun save(entity: RequestEntity): RequestEntity {
        return dynamoDbDao.save(entity)
    }

    override fun findAll(): List<RequestEntity> {
        return dynamoDbDao.findAll(RequestEntity::class.java)
    }

    override fun delete(vararg entities: RequestEntity) {
        dynamoDbDao.delete(*entities)
    }

    override fun findByHashKey(hashKey: UUID): RequestEntity? {
        return dynamoDbDao.find(RequestEntity::class.java, hashKey)
    }

    override fun deleteByHashKey(hashKey: UUID) {
        dynamoDbDao.delete(RequestEntity::class.java, hashKey)
    }

    override fun count(): Int =
        dynamoDbDao.count(RequestEntity::class.java)

}
