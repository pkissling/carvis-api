package cloud.carvis.api.service

import cloud.carvis.api.dao.repositories.CarRepository
import cloud.carvis.api.events.service.CarvisCommandPublisher
import cloud.carvis.api.mapper.CarMapper
import cloud.carvis.api.model.dtos.CarDto
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
    private val carMapper: CarMapper,
    private val commandPublisher: CarvisCommandPublisher
) {

    private val logger = KotlinLogging.logger {}

    fun fetchAllCars(): List<CarDto> {
        return carRepository.findAll()
            .map { carMapper.toDto(it) }
            .toList()
    }

    fun fetchCar(carId: UUID): CarDto {

        val car = carRepository.findByIdOrNull(carId)
        if (car == null) {
            logger.info { "Car with id [$carId] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        return car.let { carMapper.toDto(it) }
    }

    fun createCar(car: CarDto): CarDto {
        return carMapper.toEntity(car)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
    }

    @PreAuthorize("@authorization.canModifyCar(#carId)")
    fun updateCar(carId: UUID, car: CarDto): CarDto {

        val carToUpdate = carRepository.findByIdOrNull(carId)

        if (carToUpdate == null) {
            logger.info { "Car with id [$carId] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        val removeImageIds = carToUpdate.images.filter { !car.images.contains(it) }
        if (removeImageIds.isNotEmpty()) {
            commandPublisher.deleteImages(removeImageIds)
        }

        return carMapper.forUpdate(carId, car, carToUpdate)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
    }

    @PreAuthorize("@authorization.canModifyCar(#carId)")
    fun deleteCar(carId: UUID) {
        val car = carRepository.findByIdOrNull(carId)
        if (car != null) {
            carRepository.deleteById(carId)
            commandPublisher.deleteImages(car.images)
        }
    }
}
