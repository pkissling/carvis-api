package cloud.carvis.api.common.config

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas.V2_COMPATIBLE
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AmazonDynamoDBConfig {

    @Bean
    fun amazonDynamoDB(@Value("\${aws.region}") region: String): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withRegion(region)
            .build()

    @Bean
    @ConditionalOnProperty("dynamodb.table-name-prefix")
    fun tableNameOverride(@Value("\${dynamodb.table-name-prefix}") prefix: String): TableNameOverride =
        TableNameOverride.withTableNamePrefix("$prefix-")


    @Bean
    @ConditionalOnBean(TableNameOverride::class)
    fun dynamoDBMapperConfig(tableNameOverride: TableNameOverride): DynamoDBMapperConfig =
        defaultDynamoDbConfig()
            .withTableNameOverride(tableNameOverride)
            .build()

    @Bean
    @ConditionalOnMissingBean(TableNameOverride::class)
    fun defaultDynamoDBMapperConfig(): DynamoDBMapperConfig =
        defaultDynamoDbConfig()
            .build()

    @Bean
    fun dynamoDbMapper(dynamoDBMapperConfig: DynamoDBMapperConfig, amazonDynamoDb: AmazonDynamoDB): DynamoDBMapper =
        DynamoDBMapper(amazonDynamoDb, dynamoDBMapperConfig)

    private fun defaultDynamoDbConfig() =
        DynamoDBMapperConfig
            .builder()
            .withConversionSchema(V2_COMPATIBLE)
}
