package cloud.carvis.backend.rest

import cloud.carvis.backend.repositories.CarEntity
import cloud.carvis.backend.service.CarService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cars")
class CarRestController(
    val carService: CarService
) {

    @GetMapping
    fun cars(): List<CarEntity> = carService.findAll()
}