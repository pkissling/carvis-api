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

    fun findAll(): List<CarDto> {
        logger.info { "start findAll()" }
        return carRepository.findAll()
            .map { carMapper.toDto(it) }
            .toList()
            .also { logger.info { "end findAll() return=${it.size}" } }
    }

    fun findCar(id: UUID): CarDto {
        logger.info { "start findCar(id=$id)" }

        val car = carRepository.findByIdOrNull(id)
        if (car == null) {
            logger.info { "Car with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        return car
            .let { carMapper.toDto(it) }
            .also { logger.info { "end findCar(id=$id) return=${it}" } }
    }

    fun createCar(car: CarDto): CarDto {
        logger.info { "start createCar(car=$car)" }
        return carMapper.toEntity(car)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
            .also { logger.info { "end createCar(car=$car), return=${it}" } }
    }

    @PreAuthorize("@authorization.canAccessCar(#id)")
    fun updateCar(id: UUID, car: CarDto): CarDto {
        logger.info { "start updateCar(id=$id,car=$car)" }
        val carToUpdate = carRepository.findByIdOrNull(id)

        if (carToUpdate == null) {
            logger.info { "Car with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        // what happens if images ger deleted during update?
        return carMapper.forUpdate(id, car, carToUpdate)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
            .also { logger.info { "end updateCar(id,$id,car=$car), return=${it}" } }
    }

    @PreAuthorize("@authorization.canAccessCar(#id)")
    fun deleteCar(id: UUID) {
        logger.info { "start deleteCar(id=$id)" }
        val exists = carRepository.existsById(id)

        if (!exists) {
            logger.info { "Car with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        carRepository.deleteById(id)
        logger.info { "end deleteCar(id=$id)" }
    }
}
