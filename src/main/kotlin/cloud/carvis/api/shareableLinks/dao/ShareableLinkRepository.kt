package cloud.carvis.api.shareableLinks.dao

import cloud.carvis.api.common.dao.BaseRepository
import cloud.carvis.api.common.dao.DynamoDbDao
import cloud.carvis.api.shareableLinks.model.ShareableLinkEntity
import cloud.carvis.api.shareableLinks.model.ShareableLinkReference
import org.springframework.stereotype.Repository

@Repository
class ShareableLinkRepository(
    private val dynamoDbDao: DynamoDbDao<ShareableLinkEntity, ShareableLinkReference>
) : BaseRepository<ShareableLinkEntity, ShareableLinkReference> {
    override fun save(entity: ShareableLinkEntity): ShareableLinkEntity {
        return dynamoDbDao.save(entity)
    }

    override fun findAll(): List<ShareableLinkEntity> {
        return dynamoDbDao.findAll(ShareableLinkEntity::class.java)
    }

    override fun findByHashKey(hashKey: ShareableLinkReference): ShareableLinkEntity? {
        return dynamoDbDao.find(ShareableLinkEntity::class.java, hashKey)
    }

    override fun deleteByHashKey(hashKey: ShareableLinkReference) {
        dynamoDbDao.delete(ShareableLinkEntity::class.java, hashKey)
    }

    override fun count(): Int =
        dynamoDbDao.count(ShareableLinkEntity::class.java)

}
