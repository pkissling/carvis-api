package cloud.carvis.backend.service

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.mapper.CarMapper
import cloud.carvis.backend.model.dtos.CarDto
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class CarService(
    private val carRepository: CarRepository,
    private val carMapper: CarMapper
) {

    private val logger = KotlinLogging.logger {}

    fun fetchAllCars(): List<CarDto> {
        return carRepository.findAll()
            .map { carMapper.toDto(it) }
            .toList()
    }

    fun fetchCar(id: UUID): CarDto {

        val car = carRepository.findByIdOrNull(id)
        if (car == null) {
            logger.info { "Car with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        return car
            .let { carMapper.toDto(it) }
    }

    fun createCar(car: CarDto): CarDto {
        return carMapper.toEntity(car)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
    }

    @PreAuthorize("@authorization.canAccessCar(#id)")
    fun updateCar(id: UUID, car: CarDto): CarDto {

        val carToUpdate = carRepository.findByIdOrNull(id)

        if (carToUpdate == null) {
            logger.info { "Car with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        // what happens if images ger deleted during update?
        return carMapper.forUpdate(id, car, carToUpdate)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
    }

    @PreAuthorize("@authorization.canAccessCar(#id)")
    fun deleteCar(id: UUID) {
        val exists = carRepository.existsById(id)

        if (!exists) {
            logger.info { "Car with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        // TODO delete images?
        carRepository.deleteById(id)
    }
}
