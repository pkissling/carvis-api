package cloud.carvis.api.rest

import cloud.carvis.api.model.dtos.CarDto
import cloud.carvis.api.service.CarService
import mu.KotlinLogging
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/cars")
class CarRestController(
    val carService: CarService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun fetchAllCars(): List<CarDto> {
        logger.info { "start fetchAllCars()" }
        return carService.fetchAllCars()
            .also { logger.info { "end fetchAllCars() return=${it.size}" } }

    }

    @GetMapping("/{id}")
    fun fetchCar(@PathVariable id: UUID): CarDto {
        logger.info { "start fetchCar(id=$id)" }
        return carService.fetchCar(id)
            .also { logger.info { "end fetchCar(id=$id) return=${it}" } }
    }

    @PostMapping
    fun createCar(@Valid @RequestBody car: CarDto): CarDto {
        logger.info { "start createCar(car=$car)" }
        return carService.createCar(car)
            .also { logger.info { "end createCar(car=$car), return=${it}" } }
    }

    @PutMapping("/{id}")
    fun updateCar(@PathVariable id: UUID, @Valid @RequestBody car: CarDto): CarDto {
        logger.info { "start updateCar(id=$id,car=$car)" }
        return carService.updateCar(id, car)
            .also { logger.info { "end updateCar(id=$id,car=$car), return=${it}" } }
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteCar(@PathVariable id: UUID) {
        logger.info { "start deleteCar(id=$id)" }
        carService.deleteCar(id)
        logger.info { "end deleteCar(id=$id)" }
    }
}
