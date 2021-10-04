package cloud.carvis.backend.service

import cloud.carvis.backend.repositories.CarEntity
import cloud.carvis.backend.repositories.CarRepository
import org.springframework.stereotype.Service

@Service
class CarService(
    val carRepository: CarRepository
) {

    fun findAll(): List<CarEntity> = carRepository.findAll().toList()
}