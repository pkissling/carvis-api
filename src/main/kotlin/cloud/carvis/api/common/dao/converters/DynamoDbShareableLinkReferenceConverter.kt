package cloud.carvis.api.common.dao.converters

import cloud.carvis.api.shareableLinks.model.ShareableLinkReference
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter

class DynamoDbShareableLinkReferenceConverter : DynamoDBTypeConverter<String, ShareableLinkReference> {

    override fun convert(obj: ShareableLinkReference?): String? = obj?.toString()
    override fun unconvert(obj: String?): ShareableLinkReference? {
        if (obj == null) {
            return null
        }

        return ShareableLinkReference(obj)
    }
}
