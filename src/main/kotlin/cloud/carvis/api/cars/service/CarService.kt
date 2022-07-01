package cloud.carvis.api.cars.service

import cloud.carvis.api.cars.dao.CarRepository
import cloud.carvis.api.cars.mapper.CarMapper
import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.common.commands.publisher.CarvisCommandPublisher
import mu.KotlinLogging
import org.springframework.http.HttpStatus
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

    fun fetchCar(carId: UUID): CarDto? {
        return carRepository.findByHashKey(carId)
            ?.let { carMapper.toDto(it) }
    }

    fun createCar(car: CarDto): CarDto {
        return carMapper.toEntity(car)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
            .also { commandPublisher.assignImagesToCar(it.id ?: throw RuntimeException("Saved car has no ID"), it.images) }
    }


    fun updateCar(carId: UUID, car: CarDto): CarDto {
        val carToUpdate = carRepository.findByHashKey(carId)

        if (carToUpdate == null) {
            logger.info { "Car with id [$carId] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "car not found")
        }

        val addedImageIds = car.images.filter { !carToUpdate.images.contains(it) }
        if (addedImageIds.isNotEmpty()) {
            commandPublisher.assignImagesToCar(carId, addedImageIds)
        }
        val removeImageIds = carToUpdate.images.filter { !car.images.contains(it) }
        if (removeImageIds.isNotEmpty()) {
            commandPublisher.deleteImages(removeImageIds)
        }

        return carMapper.forUpdate(carId, car, carToUpdate)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
    }

    fun deleteCar(carId: UUID) {
        val car = carRepository.findByHashKey(carId)
        if (car != null) {
            carRepository.deleteByHashKey(carId)
            commandPublisher.deleteImages(car.images)
        }
    }

    fun carsCount() = carRepository.count()

}
