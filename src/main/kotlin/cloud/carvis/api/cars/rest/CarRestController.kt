package cloud.carvis.api.cars.rest

import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.cars.service.CarService
import mu.KotlinLogging
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.security.access.prepost.PreAuthorize
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

    @GetMapping("/{carId}")
    fun fetchCar(@PathVariable carId: UUID): CarDto {
        logger.info { "start fetchCar(carId=$carId)" }
        return carService.fetchCar(carId)
            .also { logger.info { "end fetchCar(carId=$carId) return=${it}" } }
    }

    @PostMapping
    fun createCar(@Valid @RequestBody car: CarDto): CarDto {
        logger.info { "start createCar(car=$car)" }
        return carService.createCar(car)
            .also { logger.info { "end createCar(car=$car), return=${it}" } }
    }

    @PutMapping("/{carId}")
    @PreAuthorize("@authorization.canModifyCar(#carId)")
    fun updateCar(@PathVariable carId: UUID, @Valid @RequestBody car: CarDto): CarDto {
        logger.info { "start updateCar(carId=$carId,car=$car)" }
        return carService.updateCar(carId, car)
            .also { logger.info { "end updateCar(carId=$carId,car=$car), return=${it}" } }
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{carId}")
    @PreAuthorize("@authorization.canModifyCar(#carId)")
    fun deleteCar(@PathVariable carId: UUID) {
        logger.info { "start deleteCar(carId=$carId)" }
        carService.deleteCar(carId)
        logger.info { "end deleteCar(carId=$carId)" }
    }
}
