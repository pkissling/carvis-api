package cloud.carvis.api.cars.dao

import cloud.carvis.api.cars.model.CarEntity
import cloud.carvis.api.common.dao.BaseRepository
import cloud.carvis.api.common.dao.DynamoDbDao
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CarRepository(
    private val dynamoDbDao: DynamoDbDao<CarEntity, UUID>
) : BaseRepository<CarEntity, UUID> {

    override fun save(entity: CarEntity): CarEntity {
        return dynamoDbDao.save(entity)
    }

    override fun findAll(): List<CarEntity> {
        return dynamoDbDao.findAll(CarEntity::class.java)
    }

    override fun delete(vararg entities: CarEntity) {
        dynamoDbDao.delete(*entities)
    }

    override fun findByHashKey(hashKey: UUID): CarEntity? {
        return dynamoDbDao.find(CarEntity::class.java, hashKey)
    }

    override fun deleteByHashKey(hashKey: UUID) {
        dynamoDbDao.delete(CarEntity::class.java, hashKey)
    }

    override fun count(): Int =
        dynamoDbDao.count(CarEntity::class.java)

}
