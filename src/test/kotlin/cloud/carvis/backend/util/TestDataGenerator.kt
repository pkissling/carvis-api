package cloud.carvis.backend.util

import cloud.carvis.backend.repositories.CarRepository
import org.springframework.stereotype.Service

@Service
class TestDataGenerator(
    private val carRepository: CarRepository
) {

    fun withEmptyDb(): TestDataGenerator {
        carRepository.deleteAll()
        return this
    }

}