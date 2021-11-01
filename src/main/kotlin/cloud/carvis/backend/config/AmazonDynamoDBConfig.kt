package cloud.carvis.backend.config

import cloud.carvis.backend.dao.repositories.CarRepository
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride
import org.socialsignin.spring.data.dynamodb.config.EnableDynamoDBAuditing
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*


@Configuration
@EnableDynamoDBAuditing
@EnableDynamoDBRepositories(basePackageClasses = [CarRepository::class])
class AmazonDynamoDBConfig {

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withRegion("eu-west-1")
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
        DynamoDBMapperConfig.builder()
            .withTableNameOverride(tableNameOverride)
            .build()

}
