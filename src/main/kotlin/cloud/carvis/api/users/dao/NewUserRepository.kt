package cloud.carvis.api.users.dao

import cloud.carvis.api.common.dao.BaseRepository
import cloud.carvis.api.common.dao.DynamoDbDao
import cloud.carvis.api.users.model.NewUserEntity
import org.springframework.stereotype.Repository

@Repository
class NewUserRepository(
    private val dynamoDbDao: DynamoDbDao<NewUserEntity, String>
) : BaseRepository<NewUserEntity, String> {
    override fun save(entity: NewUserEntity): NewUserEntity {
        return dynamoDbDao.save(entity)
    }

    override fun findAll(): List<NewUserEntity> {
        return dynamoDbDao.findAll(NewUserEntity::class.java)
    }

    override fun delete(vararg entities: NewUserEntity) {
        dynamoDbDao.delete(*entities)
    }

    override fun findByHashKey(hashKey: String): NewUserEntity? {
        return dynamoDbDao.find(NewUserEntity::class.java, hashKey)
    }

    override fun deleteByHashKey(hashKey: String) {
        dynamoDbDao.delete(NewUserEntity::class.java, hashKey)
    }

    override fun count(): Int =
        dynamoDbDao.count(NewUserEntity::class.java)
}
