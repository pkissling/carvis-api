package cloud.carvis.backend.rest

import cloud.carvis.backend.model.dtos.CarDto
import cloud.carvis.backend.service.CarService
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/cars")
class CarRestController(
    val carService: CarService
) {

    @GetMapping
    fun cars(): List<CarDto> = carService.findAll()

    @GetMapping("/{id}")
    fun car(@PathVariable id: UUID): CarDto =
        carService.findCar(id)

    @PostMapping
    fun createCar(@RequestBody car: CarDto): CarDto =
        carService.createCar(car)

    @PutMapping("/{id}")
    fun updateCar(@PathVariable id: UUID, @Valid @RequestBody car: CarDto): CarDto =
        carService.updateCar(id, car)

    @DeleteMapping("/{id}")
    fun deleteCar(@PathVariable id: UUID) =
        carService.deleteCar(id)

}
