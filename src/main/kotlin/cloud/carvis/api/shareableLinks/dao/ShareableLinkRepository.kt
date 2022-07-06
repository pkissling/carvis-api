package cloud.carvis.api.shareableLinks.dao

import cloud.carvis.api.common.dao.BaseRepository
import cloud.carvis.api.common.dao.DynamoDbDao
import cloud.carvis.api.shareableLinks.model.ShareableLinkEntity
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ShareableLinkRepository(
    private val dynamoDbDao: DynamoDbDao<ShareableLinkEntity, String>
) : BaseRepository<ShareableLinkEntity, String> {
    override fun save(entity: ShareableLinkEntity): ShareableLinkEntity {
        return dynamoDbDao.save(entity)
    }

    override fun findAll(): List<ShareableLinkEntity> =
        dynamoDbDao.findAll(ShareableLinkEntity::class.java)

    override fun delete(vararg entities: ShareableLinkEntity) =
        dynamoDbDao.delete(*entities)

    override fun findByHashKey(hashKey: String): ShareableLinkEntity? {
        return dynamoDbDao.find(ShareableLinkEntity::class.java, hashKey)
    }

    override fun deleteByHashKey(hashKey: String) {
        dynamoDbDao.delete(ShareableLinkEntity::class.java, hashKey)
    }

    override fun count(): Int =
        dynamoDbDao.count(ShareableLinkEntity::class.java)

    fun findByCarId(carId: UUID): List<ShareableLinkEntity> =
        dynamoDbDao.findByAttribute(ShareableLinkEntity::class.java, "carId" to AttributeValue().withS(carId.toString()))

}
