package cloud.carvis.api.common.dao.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.util.concurrent.atomic.AtomicLong

class DynamoDbAtomicLongConverter : DynamoDBTypeConverter<Long, AtomicLong> {

    override fun convert(obj: AtomicLong?): Long? = obj?.get()
    override fun unconvert(obj: Long?): AtomicLong? {
        if (obj == null) {
            return null
        }

        return AtomicLong(obj)
    }
}
