package cloud.carvis.api.users.model

import cloud.carvis.api.common.dao.converters.DynamoDbInstantConverter
import cloud.carvis.api.common.dao.model.Entity
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.*

@DynamoDBTable(tableName = "carvis-new-users")
data class NewUserEntity(
    override var id: UUID? = null,

    @DynamoDBHashKey
    var userId: String? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbInstantConverter::class)
    @CreatedDate
    override var createdAt: Instant? = null,

    @DynamoDBAttribute
    @CreatedBy
    override var createdBy: String? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbInstantConverter::class)
    @LastModifiedDate
    override var updatedAt: Instant? = null,

    @DynamoDBAttribute
    @LastModifiedBy
    override var updatedBy: String? = null,

    ) : Entity()
