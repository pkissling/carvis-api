package cloud.carvis.backend.service

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.model.entities.CarEntity
import org.springframework.stereotype.Service

@Service
class CarService(
    val carRepository: CarRepository
) {

    fun findAll(): List<CarEntity> = carRepository.findAll().toList()
}