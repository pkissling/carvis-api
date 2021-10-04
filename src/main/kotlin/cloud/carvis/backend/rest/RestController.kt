package cloud.carvis.backend.rest

import cloud.carvis.backend.repositories.CarEntity
import cloud.carvis.backend.service.CarService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController(
    val carService: CarService
) {

    @GetMapping
    fun helloWorld(): String = "Hello, World"

    @GetMapping("/cars")
    fun cars(): List<CarEntity> = carService.findAll()
}