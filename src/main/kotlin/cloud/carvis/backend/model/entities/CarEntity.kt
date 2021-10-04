package cloud.carvis.backend.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable

@DynamoDBTable(tableName = "carvis-dev-cars")
data class CarEntity(
    @DynamoDBHashKey
    var id: String? = null,
    @DynamoDBAttribute
    var brand: String? = null,
    @DynamoDBAttribute
    var bodyType: String? = null,
    @DynamoDBAttribute
    var ads: List<String?> = emptyList(),
    @DynamoDBAttribute
    var additionalEquipment: String? = null
)
