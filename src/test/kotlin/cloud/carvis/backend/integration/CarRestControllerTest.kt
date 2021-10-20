package cloud.carvis.backend.integration

import cloud.carvis.backend.util.AbstractApplicationTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.number.IsCloseTo.closeTo
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class CarRestControllerTest : AbstractApplicationTest() {

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
            .getCar()!!

        // when / then
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(1)))
            .andExpect(jsonPath("$.[0].id", equalTo(car.id.toString())))
            .andExpect(jsonPath("$.[0].brand", equalTo(car.brand)))
            .andExpect(jsonPath("$.[0].bodyType", equalTo(car.bodyType)))
            .andExpect(jsonPath("$.[0].ads", equalTo(car.ads)))
            .andExpect(jsonPath("$.[0].additionalEquipment", equalTo(car.additionalEquipment)))
            .andExpect(jsonPath("$.[0].capacity", equalTo(car.capacity)))
            .andExpect(jsonPath("$.[0].colorAndMaterialInterior", equalTo(car.colorAndMaterialInterior)))
            .andExpect(jsonPath("$.[0].colorExterior", equalTo(car.colorExterior)))
            .andExpect(jsonPath("$.[0].colorExteriorManufacturer", equalTo(car.colorExteriorManufacturer)))
            .andExpect(jsonPath("$.[0].condition", equalTo(car.condition)))
            .andExpect(jsonPath("$.[0].countryOfOrigin", equalTo(car.countryOfOrigin)))
            .andExpect(jsonPath("$.[0].createdAt", equalTo(car.createdAt.toString())))
            .andExpect(jsonPath("$.[0].description", equalTo(car.description)))
            .andExpect(jsonPath("$.[0].horsePower", equalTo(car.horsePower)))
            .andExpect(jsonPath("$.[0].images.length()", equalTo(car.images.size)))
            .andExpect(jsonPath("$.[0].mileage", equalTo(car.mileage)))
            .andExpect(jsonPath("$.[0].modelDetails", equalTo(car.modelDetails)))
            .andExpect(jsonPath("$.[0].modelSeries", equalTo(car.modelSeries)))
            .andExpect(jsonPath("$.[0].modelYear", equalTo(car.modelYear)))
            .andExpect(jsonPath("$.[0].ownerName", equalTo(car.ownerName)))
            .andExpect(jsonPath("$.[0].ownerUsername", equalTo(car.ownerUsername)))
            .andExpect(jsonPath("$.[0].price", closeTo(car.price!!, 0.1)))
            .andExpect(jsonPath("$.[0].transmission", equalTo(car.transmission)))
            .andExpect(jsonPath("$.[0].type", equalTo(car.type)))
            .andExpect(jsonPath("$.[0].updatedAt", equalTo(car.updatedAt.toString())))
            .andExpect(jsonPath("$.[0].vin", equalTo(car.vin)))
    }
}
