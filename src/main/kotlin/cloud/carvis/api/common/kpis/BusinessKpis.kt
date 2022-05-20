package cloud.carvis.api.common.kpis

import cloud.carvis.api.cars.service.CarService
import cloud.carvis.api.images.service.ImageService
import cloud.carvis.api.requests.service.RequestService
import cloud.carvis.api.users.service.UserService
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class BusinessKpis(
    private val imageService: ImageService,
    private val requestService: RequestService,
    private val carService: CarService,
    private val userService: UserService,
    private val meterRegistry: MeterRegistry
) {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val BUSINESS_OBJECTS_COUNT = "business_objects_count"
        const val BUSINESS_DOMAIN = "domain"
    }

    @PostConstruct
    fun postConstruct() {
        imagesCounter()
        requestsCounter()
        carsCounter()
        usersCounter()
        newUsersCounter()
    }

    private fun imagesCounter() = register("images") { imageService.imagesCount() }

    private fun requestsCounter() = register("requests") { requestService.requestsCount() }

    private fun carsCounter() = register("cars") { carService.carsCount() }

    private fun usersCounter() = register("new_users") { userService.newUsersCount() }

    private fun newUsersCounter() = register("users") { userService.usersCount() }

    private fun register(tag: String, supplier: (() -> Number)) {
        logger.info { "Registering Business KPI: $tag" }
        Gauge.builder(BUSINESS_OBJECTS_COUNT, supplier)
            .tag(BUSINESS_DOMAIN, tag)
            .register(meterRegistry)
    }
}
