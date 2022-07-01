package cloud.carvis.api.common.dao

import cloud.carvis.api.common.dao.model.Entity
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@Component
class DynamoDbDao<T : Entity<HashKey>, HashKey>(
    private val dynamoDbMapper: DynamoDBMapper,
) {

    fun save(entity: T): T {
        val now = Instant.now()
        val username = SecurityContextHolder.getContext().authentication?.name

        val exists = this.exists(entity)
        if (!exists) {
            if (entity.createdAt == null) {
                entity.createdAt = now
            }
            if (entity.createdBy == null) {
                entity.createdBy = username
            }

            if (entity.hashKey == null) {
                entity.hashKey = entity.generateHashKey()
            }
        }

        entity.updatedAt = now
        entity.updatedBy = username

        dynamoDbMapper.save(entity)
        return entity
    }

    fun find(entity: T): T? {
        val hashKey = entity::class.declaredMemberProperties
            .filter { it.javaField != null }
            .firstOrNull { it.javaField?.annotations?.any { ann -> ann is DynamoDBHashKey } ?: false }
            ?.getter
            ?.call(entity) ?: return null

        val rangeKey = entity::class.declaredMemberProperties
            .filter { it.javaField != null }
            .firstOrNull { it.javaField?.annotations?.any { ann -> ann is DynamoDBRangeKey } ?: false }
            ?.getter
            ?.call(entity)

        val clazz = entity.javaClass
        return this.find(clazz, hashKey, rangeKey)
    }

    fun find(clazz: Class<T>, hashKey: Any, rangeKey: Any? = null): T? =
        if (rangeKey != null)
            dynamoDbMapper.load(clazz, hashKey, rangeKey)
        else {
            dynamoDbMapper.load(clazz, hashKey)
        }


    fun exists(entity: T): Boolean =
        this.find(entity) != null

    fun findAll(clazz: Class<T>): List<T> =
        dynamoDbMapper.scan(clazz, DynamoDBScanExpression())
            .toList()

    fun delete(clazz: Class<T>, hashKey: Any, rangeKey: Any? = null) {
        val entity = this.find(clazz, hashKey, rangeKey)
        dynamoDbMapper.batchDelete(entity)
    }

    fun count(clazz: Class<T>): Int =
        dynamoDbMapper.count(clazz, DynamoDBScanExpression())

}
