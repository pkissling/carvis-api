package cloud.carvis.backend.integration

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.model.dtos.CarDto
import cloud.carvis.backend.util.AbstractApplicationTest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assume.assumeTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant.now
import java.util.*


class CarRestControllerTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var carRepository: CarRepository

    @Test
    @WithMockUser
    fun `cars GET - no cars`() {
        // given
        testDataGenerator.withEmptyDb()

        // when / then
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(0)))
    }

    @Test
    @WithMockUser
    fun `cars GET - one cars`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar()
            .getCar().value()

        // when / then
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(1)))
            .andExpect(jsonPath("$.[0].id").value(car.id.toString()))
            .andExpect(jsonPath("$.[0].brand").value(car.brand))
            .andExpect(jsonPath("$.[0].bodyType").value(car.bodyType))
            .andExpect(jsonPath("$.[0].ads").value(car.ads))
            .andExpect(jsonPath("$.[0].additionalEquipment").value(car.additionalEquipment))
            .andExpect(jsonPath("$.[0].capacity").value(car.capacity))
            .andExpect(jsonPath("$.[0].colorAndMaterialInterior").value(car.colorAndMaterialInterior))
            .andExpect(jsonPath("$.[0].colorExterior").value(car.colorExterior))
            .andExpect(jsonPath("$.[0].colorExteriorManufacturer").value(car.colorExteriorManufacturer))
            .andExpect(jsonPath("$.[0].condition").value(car.condition))
            .andExpect(jsonPath("$.[0].countryOfOrigin").value(car.countryOfOrigin))
            .andExpect(jsonPath("$.[0].createdAt").value(car.createdAt.toString()))
            .andExpect(jsonPath("$.[0].description").value(car.description))
            .andExpect(jsonPath("$.[0].horsePower").value(car.horsePower))
            .andExpect(jsonPath("$.[0].images").value(car.images.map { it.toString() }))
            .andExpect(jsonPath("$.[0].mileage").value(car.mileage))
            .andExpect(jsonPath("$.[0].modelDetails").value(car.modelDetails))
            .andExpect(jsonPath("$.[0].modelSeries").value(car.modelSeries))
            .andExpect(jsonPath("$.[0].modelYear").value(car.modelYear))
            .andExpect(jsonPath("$.[0].ownerName").value(car.ownerName))
            .andExpect(jsonPath("$.[0].ownerUsername").value(car.ownerUsername))
            .andExpect(jsonPath("$.[0].price").value(car.price))
            .andExpect(jsonPath("$.[0].transmission").value(car.transmission))
            .andExpect(jsonPath("$.[0].type").value(car.type))
            .andExpect(jsonPath("$.[0].updatedAt").value(car.updatedAt.toString()))
            .andExpect(jsonPath("$.[0].vin").value(car.vin))
    }

    @Test
    @WithMockUser
    fun `car GET - not found`() {
        // given
        testDataGenerator.withEmptyDb()

        // when / then
        this.mockMvc.perform(get("/cars/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `car GET - found`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar()
            .getCar()
            .value()

        // when / then
        this.mockMvc.perform(get("/cars/{id}", car.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(car.id.toString()))
    }

    @Test
    @WithMockUser(username = "foo")
    fun `cars POST - create car success`() {
        // given
        testDataGenerator.withEmptyDb()
        val car = random(CarDto::class)
        val start = now()


        // when
        val response = this.mockMvc
            .perform(
                post("/cars")
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", APPLICATION_JSON.toString()))
            .andReturn().response
        val returnedCar = toObject<CarDto>(response)

        // then
        assertThat(carRepository.count()).isEqualTo(1)
        assertThat(returnedCar.id).isNotNull
        assertThat(returnedCar.ownerUsername).isEqualTo("foo")
        assertThat(returnedCar.createdAt).isBetween(start, now())
        assertThat(returnedCar.updatedAt).isEqualTo(returnedCar.createdAt)
    }

    @Test
    @WithMockUser(username = "foo")
    fun `cars PUT - update existing car`() {
        // given
        val start = now()
        val existingCar = testDataGenerator
            .withEmptyDb()
            .withCar()
            .setOwnerUsername("foo")
            .getCar()
            .value()
        val carId = existingCar.id.toString()
        val updatedCar = random(CarDto::class)

        // when
        val response = this.mockMvc
            .perform(
                put("/cars/{id}", carId)
                    .content(updatedCar.toJson())
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(carId))
            .andExpect(jsonPath("$.brand").value(updatedCar.value().brand))
            .andExpect(jsonPath("$.bodyType").value(updatedCar.value().bodyType))
            .andExpect(jsonPath("$.ads").value(updatedCar.value().ads))
            .andExpect(jsonPath("$.additionalEquipment").value(updatedCar.value().additionalEquipment))
            .andExpect(jsonPath("$.capacity").value(updatedCar.value().capacity))
            .andExpect(jsonPath("$.colorAndMaterialInterior").value(updatedCar.value().colorAndMaterialInterior))
            .andExpect(jsonPath("$.colorExterior").value(updatedCar.value().colorExterior))
            .andExpect(jsonPath("$.colorExteriorManufacturer").value(updatedCar.value().colorExteriorManufacturer))
            .andExpect(jsonPath("$.condition").value(updatedCar.value().condition))
            .andExpect(jsonPath("$.countryOfOrigin").value(updatedCar.value().countryOfOrigin))
            .andExpect(jsonPath("$.createdAt").value(existingCar.createdAt.toString()))
            .andExpect(jsonPath("$.description").value(updatedCar.value().description))
            .andExpect(jsonPath("$.horsePower").value(updatedCar.value().horsePower))
            .andExpect(jsonPath("$.images").value(updatedCar.value().images.map { it.toString() }))
            .andExpect(jsonPath("$.mileage").value(updatedCar.value().mileage))
            .andExpect(jsonPath("$.modelDetails").value(updatedCar.value().modelDetails))
            .andExpect(jsonPath("$.modelSeries").value(updatedCar.value().modelSeries))
            .andExpect(jsonPath("$.modelYear").value(updatedCar.value().modelYear))
            .andExpect(jsonPath("$.ownerName").value(updatedCar.value().ownerName))
            .andExpect(jsonPath("$.ownerUsername").value(existingCar.ownerUsername))
            .andExpect(jsonPath("$.price").value(updatedCar.value().price))
            .andExpect(jsonPath("$.transmission").value(updatedCar.value().transmission))
            .andExpect(jsonPath("$.type").value(updatedCar.value().type))
            .andExpect(jsonPath("$.vin").value(updatedCar.value().vin))
            .andReturn().response
        val returnedCar = toObject<CarDto>(response)

        // then
        assertThat(returnedCar.updatedAt).isBetween(start, now())
    }

    @Test
    @WithMockUser(username = "foo")
    fun `cars PUT - update other users car forbidden`() {
        // given
        val car = testDataGenerator
            .withEmptyDb()
            .withCar()
            .setOwnerUsername("bar")
            .getCar()

        // when / then
        this.mockMvc
            .perform(
                put("/cars/{id}", car.value().id.toString())
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "foo", roles = ["ADMIN"])
    fun `cars PUT - admin updates others users car`() {
        // given
        val start = now()
        val car = testDataGenerator
            .withEmptyDb()
            .withCar()
            .setOwnerUsername("bar")
            .getCar()
        assumeTrue(car.value().ownerUsername == "bar")

        // when / then
        this.mockMvc
            .perform(
                put("/cars/{id}", car.value().id.toString())
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ownerUsername").value("bar"))

        // then
        val updatedCar = carRepository.findByIdOrNull(car.value().id!!)!!
        assertThat(updatedCar.ownerUsername).isEqualTo("bar")
        assertThat(updatedCar.createdAt).isEqualTo(car.value().createdAt)
        assertThat(updatedCar.lastModifiedBy).isEqualTo("foo")
        assertThat(updatedCar.updatedAt).isBetween(start, now())
    }

}
