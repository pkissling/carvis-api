package cloud.carvis.api.util.testdata

import cloud.carvis.api.dao.repositories.CarRepository
import cloud.carvis.api.dao.repositories.NewUserRepository
import cloud.carvis.api.dao.repositories.RequestRepository
import cloud.carvis.api.model.dtos.CarDto
import cloud.carvis.api.model.dtos.ImageHeight
import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.model.entities.CarEntity
import cloud.carvis.api.model.entities.Entity
import cloud.carvis.api.model.entities.NewUserEntity
import cloud.carvis.api.model.entities.RequestEntity
import cloud.carvis.api.model.events.CarvisCommand
import cloud.carvis.api.model.events.CarvisCommandType.DELETE_IMAGE
import cloud.carvis.api.model.events.UserSignupEvent
import cloud.carvis.api.properties.S3Buckets
import cloud.carvis.api.properties.SqsQueues
import cloud.carvis.api.util.helpers.SesHelper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.tyro.oss.arbitrater.arbitrary
import com.tyro.oss.arbitrater.arbitrater
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.awaitility.Awaitility.await
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.math.absoluteValue

@Service
class TestDataGenerator(
    private val carRepository: CarRepository,
    private val amazonS3: AmazonS3,
    private val amazonDynamoDB: AmazonDynamoDB,
    private val requestRepository: RequestRepository,
    private val queueMessagingTemplate: QueueMessagingTemplate,
    val objectMapper: ObjectMapper,
    private val sqsQueues: SqsQueues,
    private val s3Buckets: S3Buckets,
    private val newUserRepository: NewUserRepository,
    private val amazonSqs: AmazonSQSAsync,
    private val sesHelper: SesHelper
) {

    private var last: Any? = null

    fun withEmptyDb(): TestDataGenerator {
        val tables = amazonDynamoDB.listTables()
            .tableNames
            .map { amazonDynamoDB.describeTable(it) }
            .map { it.table }
        tables.forEach { amazonDynamoDB.deleteTable(it.tableName) }
        await().atMost(10, SECONDS)
            .until { amazonDynamoDB.listTables().tableNames.size == 0 }
        tables
            .map {
                CreateTableRequest()
                    .withTableName(it.tableName)
                    .withKeySchema(it.keySchema)
                    .withAttributeDefinitions(it.attributeDefinitions)
                    .withProvisionedThroughput(
                        ProvisionedThroughput(
                            it.provisionedThroughput.readCapacityUnits,
                            it.provisionedThroughput.writeCapacityUnits
                        )
                    )
            }
            .forEach { amazonDynamoDB.createTable(it) }
        return this
    }

    fun withEmptyBuckets(): TestDataGenerator {
        val objects = amazonS3.listObjects(s3Buckets.images)
        objects.objectSummaries.forEach { amazonS3.deleteObject(s3Buckets.images, it.key) }
        return this
    }

    fun withCar(createdBy: String? = null, imageIds: List<UUID> = emptyList()): TestDataGenerator {
        val car = random<CarEntity>()
        if (createdBy != null) {
            car.value().createdBy = createdBy
        }
        if (imageIds.isNotEmpty()) {
            car.value().images = imageIds
        }
        save(car.value())
        return this
    }

    private fun <T : Entity> save(entity: T) {
        when (entity) {
            is CarEntity -> carRepository.save(entity)
            is RequestEntity -> requestRepository.save(entity)
            is NewUserEntity -> newUserRepository.save(entity)
            else -> throw RuntimeException("unable to save entity")
        }.also { this.last = it }
    }

    fun getCar(): TestData<CarEntity> {
        return TestData(objectMapper, getLast())
    }

    fun withImage(imageId: UUID): TestDataGenerator {
        val size = ImageHeight.ORIGINAL
        amazonS3.putObject(s3Buckets.images, "$imageId/$size", arbitrary<String>())
        this.last = Image(imageId, size)
        return this
    }

    fun withImage(): TestDataGenerator {
        return withImage(UUID.randomUUID())
    }

    fun withImage(path: String): TestDataGenerator {
        val file = File(TestDataGenerator::class.java.getResource("/images/$path")!!.file)
        val imageId = UUID.randomUUID()
        val height = ImageHeight.ORIGINAL
        amazonS3.putObject(s3Buckets.images, "$imageId/$height", file)
        this.last = Image(imageId, height)
        return this
    }

    fun getImage(): Image {
        return getLast()
    }

    final inline fun <reified T : Any> random(): TestData<T> {
        val value = T::class.arbitrater()
            .generateNulls(false)
            .useDefaultValues(false)
            .createInstance()

        return when (value) {
            is CarDto -> value.apply {
                horsePower = horsePower!!.absoluteValue
                capacity = capacity!!.absoluteValue
                mileage = mileage!!.absoluteValue
                price = price!!.abs()
            }
            is RequestDto -> value.apply {
                horsePower = horsePower!!.absoluteValue
                capacity = capacity!!.absoluteValue
                mileage = mileage!!.absoluteValue
            }
            is CarEntity -> value.apply {
                horsePower = horsePower!!.absoluteValue
                capacity = capacity!!.absoluteValue
                mileage = mileage!!.absoluteValue
            }
            else -> value
        }.let { TestData(objectMapper, it) }
    }

    private inline fun <reified T> getLast(): T {
        if (this.last !is T) {
            throw RuntimeException("last is not of correct type")
        }
        return last as T
    }

    fun withRequest(createdBy: String? = null): TestDataGenerator {
        val request = random<RequestEntity>()
        if (createdBy != null) {
            request.value().createdBy = createdBy
        }
        save(request.value())
        return this
    }

    fun getRequest(): TestData<RequestEntity> {
        return TestData(objectMapper, this.getLast())
    }

    fun withUserSignupEvent(): TestDataGenerator {
        val userSignup = random<UserSignupEvent>()
        val msg = GenericMessage(userSignup.toJson())
        queueMessagingTemplate.send(sqsQueues.userSignup, msg)
        last = userSignup.value()
        return this
    }

    fun getUserSignupEvent(): TestData<UserSignupEvent> {
        return TestData(objectMapper, this.getLast())
    }

    fun withNewUsers(count: Int): TestDataGenerator {
        val entityIds = (0 until count).map { it.toString() }.toList()
        return withNewUsers(*entityIds.toTypedArray())
    }

    fun withNewUsers(vararg userIds: String): TestDataGenerator {
        userIds.map {
            val entity = random<NewUserEntity>()
            entity.value().userId = it
            save(entity.value())
        }
        return this
    }

    fun withEmptyQueues(): TestDataGenerator {
        val queues = amazonSqs.listQueues()
        queues.queueUrls
            .map { PurgeQueueRequest().withQueueUrl(it) }
            .forEach { amazonSqs.purgeQueue(it) }
        await().atMost(30, SECONDS)
            .until {
                queues.queueUrls
                    .map { amazonSqs.receiveMessage(it) }
                    .map { it.messages }
                    .all { it.isEmpty() }
            }
        return this
    }

    fun withNoMails(): TestDataGenerator {
        sesHelper.cleanMails()
        return this
    }

    fun getUserSignupDlqMessages(): List<Message> {
        val queueUrl = amazonSqs.listQueues(sqsQueues.userSignup)
            .queueUrls
            .first { it.endsWith("_dlq") }
        return amazonSqs.receiveMessage(
            ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
        ).messages
    }

    fun withDeleteImageCommand(id: UUID): TestDataGenerator {
        val command = CarvisCommand(id, DELETE_IMAGE)
        queueMessagingTemplate.convertAndSend(sqsQueues.carvisCommand, command)
        last = command
        return this
    }

    fun getCarvisCommandDlqMessages(): List<Message> {
        val queueUrl = amazonSqs.listQueues(sqsQueues.carvisCommand)
            .queueUrls
            .first { it.endsWith("_dlq") }
        return amazonSqs.receiveMessage(
            ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
        ).messages
    }

    data class Image(
        val id: UUID,
        val height: ImageHeight
    )
}
