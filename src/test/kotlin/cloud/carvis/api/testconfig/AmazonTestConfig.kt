package cloud.carvis.api.testconfig

import cloud.carvis.api.properties.S3Buckets
import cloud.carvis.api.properties.SqsQueues
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@TestConfiguration
@Testcontainers
class AmazonTestConfig {

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(localStack.getEndpointConfiguration(DYNAMODB))
            .withCredentials(localStack.defaultCredentialsProvider)
            .build()

    // must match bean name exactly to make aws autoconfig back off
    @Bean("amazonSQS")
    fun amazonSqs(): AmazonSQSAsync =
        AmazonSQSAsyncClientBuilder.standard()
            .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
            .withCredentials(localStack.defaultCredentialsProvider)
            .build()

    @Bean
    fun amazonS3(): AmazonS3 = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(localStack.getEndpointConfiguration(S3))
        .withCredentials(localStack.defaultCredentialsProvider)
        .build()

    @Bean
    fun queueMessagingTemplate(amazonSqsAsync: AmazonSQSAsync): QueueMessagingTemplate =
        QueueMessagingTemplate(amazonSqsAsync)

    @Autowired
    fun createBuckets(s3buckets: S3Buckets, amazonS3: AmazonS3) =
        s3buckets.forEach {
            amazonS3.createBucket(it)
        }

    @Autowired
    fun createQueues(amazonSqs: AmazonSQS, sqsQueues: SqsQueues) {
        sqsQueues.forEach {
            amazonSqs.createQueue(it)
        }
    }

    companion object {

        @JvmStatic
        @Container
        var localStack = LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.1"))
            .withServices(SQS, S3, DYNAMODB)
            .apply {
                this.start()
            }
    }
}
