package cloud.carvis.api.util.mocks

import cloud.carvis.api.common.properties.EmailProperties
import cloud.carvis.api.common.properties.S3Buckets
import cloud.carvis.api.common.properties.SqsQueues
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.assertj.core.api.Assertions.assertThat
import org.reflections.Reflections
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField


@TestConfiguration
@Testcontainers
class AwsMock {

    @Bean
    fun localStack(): CarvisLocalStack =
        CarvisLocalStack(localStackContainer, tempDir)

    companion object {

        private val tempDir = createTempDirectory()

        @JvmStatic
        @Container
        var localStackContainer: LocalStackContainer =
            LocalStackContainer(DockerImageName.parse("localstack/localstack:0.14.2"))
                .withServices(SQS, S3, DYNAMODB, SES)
                .withEnv("DATA_DIR", "/data")
                .withFileSystemBind(tempDir.pathString, "/data")
                .apply {
                    this.start()
                }
    }

    @Configuration
    class DynamoDb {

        @Autowired
        lateinit var tableNameOverride: TableNameOverride

        @Bean
        fun amazonDynamoDB(localStack: CarvisLocalStack): AmazonDynamoDB =
            AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(DYNAMODB))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .build()
                .also { createTables(it) }

        private fun createTables(dynamoDb: AmazonDynamoDB) {
            Reflections("cloud.carvis.api")
                .getTypesAnnotatedWith(DynamoDBTable::class.java)
                .map { it.kotlin }
                .map {
                    val tableName = fetchTableName(it)
                    val hashKey = extractHashKeyAttributeName(it)
                    CreateTableRequest()
                        .withTableName(tableName)
                        .withAttributeDefinitions(
                            AttributeDefinition()
                                .withAttributeType(ScalarAttributeType.S)
                                .withAttributeName(hashKey)
                        )
                        .withKeySchema(
                            KeySchemaElement()
                                .withKeyType(KeyType.HASH)
                                .withAttributeName(hashKey)
                        )
                        .withProvisionedThroughput(
                            ProvisionedThroughput()
                                .withReadCapacityUnits(1L)
                                .withWriteCapacityUnits(1L)
                        )
                }
                .forEach { dynamoDb.createTable(it) }
        }


        private fun fetchTableName(clazz: KClass<*>) =
            clazz.findAnnotation<DynamoDBTable>()
                ?.tableName
                ?.let { tableNameOverride.tableNamePrefix + it }
                ?: throw RuntimeException("could not extract tableName from $clazz")

        private fun extractHashKeyAttributeName(clazz: KClass<*>) =
            clazz.declaredMemberProperties
                .filter { it.javaField != null }
                .firstOrNull { it.javaField?.annotations?.any { ann -> ann is DynamoDBHashKey } ?: false }
                ?.name
                ?: throw RuntimeException("unable to extract hashKey")
    }

    @Configuration
    class Sqs {

        // must match bean name exactly to make aws autoconfig back off
        @Bean("amazonSQS")
        fun amazonSqs(localStack: CarvisLocalStack, sqsQueues: SqsQueues): AmazonSQSAsync =
            AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .build()
                .also { createQueues(sqsQueues, it) }

        private fun createQueues(sqsQueues: SqsQueues, amazonSqs: AmazonSQSAsync) {
            sqsQueues.forEach { queueName ->
                val dlqArn = createDlq(amazonSqs, queueName)
                createQueue(amazonSqs, queueName, dlqArn)
            }
        }

        private fun createQueue(amazonSqs: AmazonSQSAsync, queueName: String, dlqArn: String) {
            amazonSqs.createQueue(
                CreateQueueRequest()
                    .withQueueName(queueName)
                    .withAttributes(
                        mapOf(
                            "RedrivePolicy" to """{ "maxReceiveCount": 1, "deadLetterTargetArn": "$dlqArn" }""",
                            "VisibilityTimeout" to "0"
                        )
                    )
            )
        }

        private fun createDlq(amazonSqs: AmazonSQSAsync, queueName: String): String {
            val dlq = amazonSqs.createQueue("${queueName}_dlq")
            return amazonSqs.getQueueAttributes(
                GetQueueAttributesRequest(dlq.queueUrl)
                    .withAttributeNames("QueueArn")
            )
                .attributes["QueueArn"]
                ?: throw RuntimeException("QueueArn required")
        }

        @Bean
        fun queueMessagingTemplate(amazonSqsAsync: AmazonSQSAsync, objectMapper: ObjectMapper): QueueMessagingTemplate =
            QueueMessagingTemplate(amazonSqsAsync, null, objectMapper)
    }

    @Configuration
    class S3 {

        @Bean
        fun amazonS3(localStack: CarvisLocalStack, s3Buckets: S3Buckets): AmazonS3 = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(localStack.getEndpointConfiguration(S3))
            .withCredentials(localStack.getDefaultCredentialsProvider())
            .build()
            .also { createBuckets(s3Buckets, it) }

        private fun createBuckets(s3Buckets: S3Buckets, amazonS3: AmazonS3) =
            s3Buckets.forEach { amazonS3.createBucket(it) }
    }

    @Configuration
    class Ses {

        @Bean
        fun amazonSimpleEmailService(
            localStack: CarvisLocalStack,
            emailProperties: EmailProperties
        ): AmazonSimpleEmailService =
            AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(SES))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .build()
                .also { verifyEmail(localStack, emailProperties.userSignup.fromMail) }

        private fun verifyEmail(localStack: CarvisLocalStack, fromMail: String) {
            val exitCode = localStack.execInContainer(
                "awslocal", "ses", "verify-email-identity",
                "--email-address", fromMail, "--endpoint-url=http://localhost:4566"
            )
            assertThat(exitCode).isEqualTo(0)
        }
    }

    class CarvisLocalStack(
        private val localStackContainer: LocalStackContainer,
        private val dataDir: Path
    ) {
        fun getEndpointConfiguration(service: LocalStackContainer.Service): AwsClientBuilder.EndpointConfiguration =
            localStackContainer.getEndpointConfiguration(service)

        fun getDefaultCredentialsProvider(): AWSCredentialsProvider =
            localStackContainer.defaultCredentialsProvider

        fun execInContainer(vararg args: String): Int =
            localStackContainer.execInContainer(*args).exitCode

        fun getDataDir(): Path =
            dataDir
    }
}
