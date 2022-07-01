package cloud.carvis.api.users.model

import cloud.carvis.api.common.dao.converters.DynamoDbInstantConverter
import cloud.carvis.api.common.dao.model.Entity
import com.amazonaws.services.dynamodbv2.datamodeling.*
import org.apache.commons.lang3.RandomStringUtils
import java.time.Instant

@DynamoDBTable(tableName = "carvis-new-users")
data class NewUserEntity(

    @DynamoDBHashKey
    var userId: String? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbInstantConverter::class)
    override var createdAt: Instant? = null,

    @DynamoDBAttribute
    override var createdBy: String? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbInstantConverter::class)
    override var updatedAt: Instant? = null,

    @DynamoDBAttribute
    override var updatedBy: String? = null,

    ) : Entity<String>() {

    @get:DynamoDBIgnore
    @set:DynamoDBIgnore
    override var hashKey: String?
        get() = this.userId
        set(userId) {
            this.userId = userId
        }

    override fun generateHashKey(): String =
        RandomStringUtils.random(16, true, false)

}
