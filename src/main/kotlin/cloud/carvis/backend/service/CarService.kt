package cloud.carvis.backend.service

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.mapper.CarMapper
import cloud.carvis.backend.model.dtos.CarDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class CarService(
    private val carRepository: CarRepository,
    private val carMapper: CarMapper
) {

    fun findAll(): List<CarDto> {
        return carRepository.findAll()
            .map { carMapper.fromEntity(it) }
            .toList()
    }

    fun findCar(id: UUID): CarDto? =
        carRepository.findByIdOrNull(id)
            ?.let { carMapper.fromEntity(it) }
}
