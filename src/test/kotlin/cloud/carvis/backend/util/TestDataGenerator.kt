package cloud.carvis.backend.util

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.model.entities.CarEntity
import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.services.s3.AmazonS3
import com.tyro.oss.arbitrater.arbitrary
import com.tyro.oss.arbitrater.arbitrater
import org.springframework.stereotype.Service

@Service
class TestDataGenerator(
    private val carRepository: CarRepository,
    private val amazonS3: AmazonS3,
    s3Properties: S3Properties
) {

    private var last: Any? = null
    private val imagesBucket = s3Properties.bucketNames["images"]

    fun withEmptyDb(): TestDataGenerator {
        carRepository.deleteAll()
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

        car.images.forEach { this.uploadImage("$it/200") }
        return this
    }

    fun uploadImage(fileName: String): TestDataGenerator {
        amazonS3.putObject(imagesBucket, fileName, arbitrary<String>())
        return this
    }

    fun getCar(): CarEntity? {
        return last as CarEntity?
    }

}