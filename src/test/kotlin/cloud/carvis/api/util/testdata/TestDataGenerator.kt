package cloud.carvis.api.util.testdata

import cloud.carvis.api.cars.dao.CarRepository
import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.cars.model.CarEntity
import cloud.carvis.api.common.commands.model.AssignImageToCarCommand
import cloud.carvis.api.common.commands.model.CarvisCommand
import cloud.carvis.api.common.commands.model.DeleteImageCommand
import cloud.carvis.api.common.dao.model.Entity
import cloud.carvis.api.common.events.model.CarDeletedEvent
import cloud.carvis.api.common.events.model.CarvisEvent
import cloud.carvis.api.common.events.model.UserSignupEvent
import cloud.carvis.api.common.properties.S3Buckets
import cloud.carvis.api.common.properties.SqsQueues
import cloud.carvis.api.images.model.ImageHeight
import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.requests.dao.RequestRepository
import cloud.carvis.api.requests.model.entities.RequestEntity
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import cloud.carvis.api.shareableLinks.model.ShareableLinkEntity
import cloud.carvis.api.users.dao.NewUserRepository
import cloud.carvis.api.users.model.NewUserEntity
import cloud.carvis.api.util.helpers.SesHelper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.tyro.oss.arbitrater.arbitrary
import com.tyro.oss.arbitrater.arbitrater
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.Awaitility.await
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicLong
import kotlin.Long.Companion.MAX_VALUE
import kotlin.math.absoluteValue
import kotlin.reflect.KClass
import kotlin.reflect.jvm.ExperimentalReflectionOnLambdas

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
    private val sesHelper: SesHelper,
    private val shareableLinkRepository: ShareableLinkRepository
) {

    private var last: MutableMap<KClass<*>, Any> = mutableMapOf()

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

    fun withDeletedImage(): TestDataGenerator {
        return this.withImage(prefix = "deleted")
    }

    fun withImage(
        prefix: String? = null,
        imageId: UUID = UUID.randomUUID(),
        height: ImageHeight = ImageHeight.ORIGINAL,
        testFilePath: String? = null
    ): TestDataGenerator {
        val key = prefix?.let { "$it/$imageId/$height" } ?: "$imageId/$height"
        if (testFilePath == null) {
            amazonS3.putObject(s3Buckets.images, key, arbitrary<String>())
        } else {
            val file = File(TestDataGenerator::class.java.getResource("/images/$testFilePath")!!.file)
            amazonS3.putObject(s3Buckets.images, key, file)
        }
        this.last[Image::class] = Image(imageId, height)
        return this
    }

    fun getImage(): Image {
        return getLast(Image::class)
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

    fun getCar(): TestData<CarEntity> {
        return TestData(objectMapper, getLast(CarEntity::class))
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
        return TestData(objectMapper, this.getLast(RequestEntity::class))
    }

    @OptIn(ExperimentalReflectionOnLambdas::class)
    final inline fun <reified T : Any> random(): TestData<T> {
        val value = T::class.arbitrater()
            .generateNulls(false)
            .useDefaultValues(false)
            .apply {
                registerGenerator { AtomicLong(ThreadLocalRandom.current().nextLong(0, MAX_VALUE)) }
            }
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

            is ShareableLinkEntity -> value.apply {
                shareableLinkReference = RandomStringUtils.random(8, true, false)
            }

            else -> value
        }.let { TestData(objectMapper, it) }
    }

    private inline fun <reified T> getLast(clazz: KClass<*>): T {
        val last = this.last[clazz]
            ?: throw RuntimeException("last is not of correct type")
        return last as T
    }

    fun withUserSignupEvent(): TestDataGenerator {
        val userSignup = random<UserSignupEvent>()
        queueMessagingTemplate.convertAndSend(sqsQueues.userSignup, userSignup.value())
        addLast(userSignup.value())
        return this
    }

    fun getUserSignupEvent(): TestData<UserSignupEvent> {
        return TestData(objectMapper, this.getLast(UserSignupEvent::class))
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
                    .map { (getQueueMessageCount(it) ?: 0) + amazonSqs.receiveMessage(it).messages.size }
                    .all { it == 0 }
            }
        return this
    }

    fun withNoMails(): TestDataGenerator {
        sesHelper.cleanMails()
        return this
    }

    fun withAssignImageToCarCommand(carId: UUID = UUID.randomUUID(), imageId: UUID = UUID.randomUUID()): TestDataGenerator {
        return withCarvisCommand(AssignImageToCarCommand(carId, imageId))
    }

    fun withDeleteImageCommand(id: UUID = UUID.randomUUID()): TestDataGenerator {
        return withCarvisCommand(DeleteImageCommand(id))
    }

    fun getCarvisCommandDlqMessageCount(): Int? {
        val queueUrl = getQueueUrl(sqsQueues.carvisCommand, true)
        return getQueueMessageCount(queueUrl)
    }

    fun getCarvisCommandMessageCount(): Int? {
        val queueUrl = getQueueUrl(sqsQueues.carvisCommand, false)
        return getQueueMessageCount(queueUrl)
    }

    fun getUserSignupMessageCount(): Int? {
        val queueUrl = getQueueUrl(sqsQueues.userSignup, false)
        return getQueueMessageCount(queueUrl)
    }

    fun getUserSignupDlqMessageCount(): Int? {
        val queueUrl = getQueueUrl(sqsQueues.userSignup, true)
        return getQueueMessageCount(queueUrl)
    }

    fun getCarvisEventMessageCount(): Int? {
        val queueUrl = getQueueUrl(sqsQueues.carvisEvent, false)
        return getQueueMessageCount(queueUrl)
    }

    fun getCarvisEventDlqMessageCount(): Int? {
        val queueUrl = getQueueUrl(sqsQueues.carvisEvent, true)
        return getQueueMessageCount(queueUrl)
    }

    fun withShareableLink(carId: UUID = UUID.randomUUID()): TestDataGenerator {
        val shareableLink = random<ShareableLinkEntity>().apply {
            value().carId = carId
        }
        this.save(shareableLink.value())
        return this
    }

    fun getSharedLinkReference(): TestData<ShareableLinkEntity> {
        return TestData(objectMapper, this.getLast(ShareableLinkEntity::class))
    }

    fun withCarDeletedEvent(carId: UUID = UUID.randomUUID(), imageIds: List<UUID> = emptyList()): TestDataGenerator {
        return withCarvisEvent(CarDeletedEvent(carId, imageIds))
    }

    private fun getQueueUrl(queueName: String, isDlq: Boolean) =
        amazonSqs.listQueues(queueName)
            .queueUrls
            .first { it.endsWith("_dlq") == isDlq }

    private fun <T : Entity<*>> save(entity: T) {
        when (entity) {
            is CarEntity -> carRepository.save(entity)
            is RequestEntity -> requestRepository.save(entity)
            is NewUserEntity -> newUserRepository.save(entity)
            is ShareableLinkEntity -> shareableLinkRepository.save(entity)
            else -> throw RuntimeException("unable to save entity")
        }.also { addLast(it) }
    }

    private fun withCarvisCommand(command: CarvisCommand): TestDataGenerator {
        queueMessagingTemplate.convertAndSend(sqsQueues.carvisCommand, command)
        addLast(command)
        return this
    }

    private fun withCarvisEvent(event: CarvisEvent): TestDataGenerator {
        queueMessagingTemplate.convertAndSend(sqsQueues.carvisEvent, event)
        addLast(event)
        return this
    }


    private fun getQueueMessageCount(queueUrl: String): Int? {
        return amazonSqs.getQueueAttributes(
            GetQueueAttributesRequest()
                .withQueueUrl(queueUrl)
                .withAttributeNames("ApproximateNumberOfMessages")
        ).attributes["ApproximateNumberOfMessages"]?.toInt()
    }

    private fun <T> addLast(last: T) {
        this.last[last!!::class] = last
    }

    data class Image(
        val id: UUID,
        val height: ImageHeight
    )
}
