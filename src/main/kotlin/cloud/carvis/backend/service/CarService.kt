package cloud.carvis.backend.service

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.mapper.CarMapper
import cloud.carvis.backend.model.dtos.CarDto
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
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
            .also { logger.info { "end findAll() return=${it.size}"} }
    }

    fun findCar(id: UUID): CarDto? {
        logger.info { "start findCar(id=$id)" }
        return carRepository.findByIdOrNull(id)
            ?.let { carMapper.toDto(it) }
            .also { logger.info { "end findCar(id=$id) return=${it}" } }
    }

    fun createCar(car: CarDto): CarDto {
        logger.info { "start createCar(car=$car)" }
        return carMapper.toEntity(car)
            .let { carRepository.save(it) }
            .let { carMapper.toDto(it) }
            .also { logger.info { "end createCar(car=$car), return=${it}" } }
    }
}
