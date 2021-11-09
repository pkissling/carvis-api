package cloud.carvis.api.testdata

import cloud.carvis.api.dao.repositories.CarRepository
import cloud.carvis.api.model.dtos.CarDto
import cloud.carvis.api.model.dtos.ImageSize
import cloud.carvis.api.model.entities.CarEntity
import cloud.carvis.api.properties.S3Properties
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest
import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.databind.ObjectMapper
import com.tyro.oss.arbitrater.arbitrary
import com.tyro.oss.arbitrater.arbitrater
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import kotlin.math.absoluteValue

@Service
class TestDataGenerator(
    private val carRepository: CarRepository,
    private val amazonS3: AmazonS3,
    private val amazonDynamoDB: AmazonDynamoDB,
    val objectMapper: ObjectMapper,
    s3Properties: S3Properties
) {

    private var last: Any? = null
    val imagesBucket = s3Properties.bucketNames["images"]

    fun withEmptyDb(): TestDataGenerator {
        amazonDynamoDB.listTables()
            .tableNames
            .flatMap {
                amazonDynamoDB.scan(it, emptyMap())
                    .items
                    .map { item -> it to item["id"]!! }
            }
            .map {
                DeleteItemRequest()
                    .withTableName(it.first)
                    .withKey(mapOf("id" to it.second))
            }
            .forEach { amazonDynamoDB.deleteItem(it) }
        return this
    }

    fun withEmptyBucket(): TestDataGenerator {
        val objects = amazonS3.listObjects(imagesBucket)
        objects.objectSummaries.forEach { amazonS3.deleteObject(imagesBucket, it.key) }
        return this
    }

    fun withCar(): TestDataGenerator {
        val car = random<CarEntity>()

        carRepository.save(car.value()).let {
            this.last = it
        }
        return this
    }

    fun setCreatedBy(createdBy: String): TestDataGenerator {
        val car = this.getCar().value()
        car.createdBy = createdBy
        this.last = carRepository.save(car)
        return this
    }

    fun getCar(): TestData<CarEntity> {
        return TestData(objectMapper, getLast())
    }

    fun withImage(): TestDataGenerator {
        val id = UUID.randomUUID()
        val size = ImageSize.ORIGINAL
        amazonS3.putObject(imagesBucket, "$id/$size", arbitrary<String>())
        this.last = Image(id, size)
        return this
    }

    fun withImage(path: String): TestDataGenerator {
        val file = File(TestDataGenerator::class.java.getResource("/images/$path")!!.file)
        val id = UUID.randomUUID()
        val size = ImageSize.ORIGINAL
        amazonS3.putObject(imagesBucket, "$id/$size", file)
        this.last = Image(id, size)
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
            is CarDto -> {
                value.apply {
                    horsePower = horsePower!!.absoluteValue
                    capacity = capacity!!.absoluteValue
                    mileage = mileage!!.absoluteValue
                    price = price!!.abs()
                }
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

    data class Image(
        val id: UUID,
        val size: ImageSize
    )
}
