package cloud.carvis.api.dao.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import mu.KotlinLogging
import java.time.Instant

class DynamoDbInstantConverter : DynamoDBTypeConverter<String, Instant> {

    private val logger = KotlinLogging.logger {}

    override fun convert(i: Instant?): String? = i?.toString()
    override fun unconvert(s: String?): Instant? {
        if (s == null) {
            return null
        }

        return try {
            Instant.parse(s)
        } catch (e: Exception) {
            logger.error(e) { "Unable to create Instant from String: $s" }
            null
        }
    }
}
