package cloud.carvis.api.common.config

import cloud.carvis.api.cars.dao.CarRepository
import cloud.carvis.api.requests.dao.RequestRepository
import cloud.carvis.api.users.dao.NewUserRepository
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas.V2_COMPATIBLE
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride
import org.socialsignin.spring.data.dynamodb.config.EnableDynamoDBAuditing
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*


@Configuration
@EnableDynamoDBAuditing
@EnableDynamoDBRepositories(basePackageClasses = [CarRepository::class, NewUserRepository::class, RequestRepository::class])
class AmazonDynamoDBConfig {

    @Bean
    fun amazonDynamoDB(@Value("\${aws.region}") region: String): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withRegion(region)
            .build()

    @Bean
    fun userAuditing(): AuditorAware<String> {
        return AuditorAware {
            Optional.ofNullable(SecurityContextHolder.getContext().authentication?.name)
        }
    }

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

    private fun defaultDynamoDbConfig() =
        DynamoDBMapperConfig
            .builder()
            .withConversionSchema(V2_COMPATIBLE)

}
