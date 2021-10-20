package cloud.carvis.backend.util

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class AmazonDynamoDbTestConfig {

    @Bean
    fun amazonDynamoDB(
        @Value("\${application.dynamodb.endpoint.ip}") ip: String,
        @Value("\${application.dynamodb.endpoint.port}") port: String
    ): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://${ip}:${port}", "eu-west-1"))
            .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("fake", "fake")))
            .build()
}