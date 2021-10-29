package cloud.carvis.backend.util

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.model.entities.CarEntity
import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.databind.ObjectMapper
import com.tyro.oss.arbitrater.arbitrary
import com.tyro.oss.arbitrater.arbitrater
import org.springframework.stereotype.Service
import java.util.*

@Service
class TestDataGenerator(
    private val carRepository: CarRepository,
    private val amazonS3: AmazonS3,
    private val objectMapper: ObjectMapper,
    s3Properties: S3Properties
) {

    private var last: Any? = null
    private val imagesBucket = s3Properties.bucketNames["images"]

    fun withEmptyDb(): TestDataGenerator {
        carRepository.deleteAll()
        return this
    }

    fun withEmptyBucket(): TestDataGenerator {
        val objects = amazonS3.listObjects(imagesBucket)
        objects.objectSummaries.forEach { amazonS3.deleteObject(imagesBucket, it.key) }
        return this
    }

    fun withCar(): TestDataGenerator {
        val car = CarEntity::class.arbitrater()
            .generateNulls(false)
            .useDefaultValues(false)
            .createInstance()

        carRepository.save(car).let {
            this.last = it
        }
        return this
    }

    fun setOwnerUsername(ownerUsername: String): TestDataGenerator {
        val car = this.getCar().value()
        car.ownerUsername = ownerUsername
        this.last = carRepository.save(car)
        return this
    }

    fun getCar(): TestData<CarEntity> {
        return TestData(objectMapper, getLast())
    }

    fun withImage(): TestDataGenerator {
        val id = UUID.randomUUID()
        val size = Random().nextInt(1000).toString()
        amazonS3.putObject(imagesBucket, "$id/$size", arbitrary<String>())
        this.last = Image(id, size)
        return this
    }

    fun getImage(): Image {
        return getLast()
    }

    private inline fun <reified T> getLast(): T {
        if (this.last !is T) {
            throw RuntimeException("last is not of correct type")
        }
        return last as T
    }

    data class Image(
        val id: UUID,
        val size: String
    )
}
