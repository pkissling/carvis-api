package cloud.carvis.api.shareableLinks.model

import cloud.carvis.api.common.dao.converters.DynamoDbAtomicLongConverter
import cloud.carvis.api.common.dao.converters.DynamoDbInstantConverter
import cloud.carvis.api.common.dao.converters.DynamoDbShareableLinkReferenceConverter
import cloud.carvis.api.common.dao.model.Entity
import com.amazonaws.services.dynamodbv2.datamodeling.*
import org.apache.commons.lang3.RandomStringUtils
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong


@DynamoDBTable(tableName = "carvis-shareable-links")
data class ShareableLinkEntity(
    @DynamoDBTypeConverted(converter = DynamoDbShareableLinkReferenceConverter::class)
    @DynamoDBHashKey
    var shareableLinkReference: ShareableLinkReference? = null,

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

    @DynamoDBAttribute
    var carId: UUID? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbAtomicLongConverter::class)
    var visitorCount: AtomicLong? = null,

    @DynamoDBAttribute
    var recipientName: String? = null,

    ) : Entity<ShareableLinkReference>() {

    @get:DynamoDBIgnore
    @set:DynamoDBIgnore
    override var hashKey: ShareableLinkReference?
        get() = this.shareableLinkReference
        set(value) {
            this.shareableLinkReference = value
        }

    override fun generateHashKey(): ShareableLinkReference =
        ShareableLinkReference(RandomStringUtils.random(8, true, false))

}
