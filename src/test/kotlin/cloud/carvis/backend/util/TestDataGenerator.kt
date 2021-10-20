package cloud.carvis.backend.util

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.model.entities.CarEntity
import com.tyro.oss.arbitrater.arbitrater
import org.springframework.stereotype.Service

@Service
class TestDataGenerator(
    private val carRepository: CarRepository
) {

    private var last: Any? = null

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
        return this
    }

    fun getCar(): CarEntity? {
        return last as CarEntity?
    }

//    @Bean
//    fun defaultConfiguration(): DefaultConfiguration {
//        arbitrater().useDefaultValues(false).withValue("id", "myid")
//    }

}