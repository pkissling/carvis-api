package cloud.carvis.backend.rest

import cloud.carvis.backend.model.dtos.CarDto
import cloud.carvis.backend.service.CarService
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*

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
            ?: throw ResponseStatusException(NOT_FOUND, "car not found")
}
