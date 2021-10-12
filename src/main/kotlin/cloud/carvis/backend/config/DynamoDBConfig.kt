package cloud.carvis.backend.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableDynamoDBRepositories("cloud.carvis.backend.repositories")
class DynamoDBConfig {

    @Bean
    fun amazonDynamoDB(
        @Value("\${application.dynamodb.endpoint.ip:undefined}") ip: String,
        @Value("\${application.dynamodb.endpoint.port:undefined}") port: String
    ): AmazonDynamoDB {

        if (ip != "undefined" && port != "undefined") {
            return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://${ip}:${port}", "eu-west-1"))
                .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("fake", "fake")))
                .build()
        }

        return AmazonDynamoDBClientBuilder.standard().build()
    }
}