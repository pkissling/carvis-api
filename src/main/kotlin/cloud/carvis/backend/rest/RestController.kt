package cloud.carvis.backend.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController {

    @GetMapping
    fun helloWorld() : String = "Hello, World"

}