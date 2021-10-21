package cloud.carvis.backend.util

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@TestConfiguration
@Testcontainers
class AmazonDynamoDbTestConfig {

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration(
                    "http://${dynamoDb.containerIpAddress}:${dynamoDb.firstMappedPort}",
                    "eu-west-1"
                )
            )
            .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("fake", "fake")))
            .build()

    companion object {

        @JvmStatic
        @Container
        val dynamoDb = GenericContainer<Nothing>("amazon/dynamodb-local")
            .apply {
                withExposedPorts(8000)
                start()
            }
    }

}