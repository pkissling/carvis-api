package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.api.dao.repositories.RequestRepository
import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.testdata.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.*


class RequestRestControllerTest : AbstractApplicationTest() {

    @Autowired
    lateinit var requestRepository: RequestRepository

    @Test
    @WithMockUser
    fun `requests GET - no requests`() {
        // given
        testDataGenerator.withEmptyDb()

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser
    fun `requests GET - with requests`() {
        // given
        testDataGenerator
            .withEmptyDb()
            .withRequest()
            .withRequest()

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `request GET - success`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest(VALID_USER_ID)
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", request.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(request.id.toString()))
            .andExpect(jsonPath("createdAt").value(request.createdAt.toString()))
            .andExpect(jsonPath("createdBy").value(request.createdBy))
            .andExpect(jsonPath("updatedAt").value(request.updatedAt.toString()))
            .andExpect(jsonPath("updatedBy").value(request.updatedBy))
            .andExpect(jsonPath("additionalEquipment").value(request.additionalEquipment))
            .andExpect(jsonPath("bodyType").value(request.bodyType))
            .andExpect(jsonPath("brand").value(request.brand))
            .andExpect(jsonPath("budget").value(request.budget))
            .andExpect(jsonPath("capacity").value(request.capacity))
            .andExpect(jsonPath("countryOfOrigin").value(request.countryOfOrigin))
            .andExpect(jsonPath("colorExterior").value(request.colorExterior))
            .andExpect(jsonPath("colorExteriorManufacturer").value(request.colorExteriorManufacturer))
            .andExpect(jsonPath("colorAndMaterialInterior").value(request.colorAndMaterialInterior))
            .andExpect(jsonPath("condition").value(request.condition))
            .andExpect(jsonPath("description").value(request.description))
            .andExpect(jsonPath("hasHiddenFields").value(false))
            .andExpect(jsonPath("highlights").value(request.highlights))
            .andExpect(jsonPath("horsePower").value(request.horsePower))
            .andExpect(jsonPath("mileage").value(request.mileage))
            .andExpect(jsonPath("mustHaves").value(request.mustHaves))
            .andExpect(jsonPath("noGos").value(request.noGos))
            .andExpect(jsonPath("modelSeries").value(request.modelSeries))
            .andExpect(jsonPath("modelYear").value(request.modelYear))
            .andExpect(jsonPath("modelSpecification").value(request.modelSpecification))
            .andExpect(jsonPath("ownerName").value(Users.VALID_USER_NAME))
            .andExpect(jsonPath("transmission").value(request.transmission))
            .andExpect(jsonPath("type").value(request.type))
            .andExpect(jsonPath("vision").value(request.vision))
    }

    @Test
    @WithMockUser
    fun `request GET - not found`() {
        // given
        testDataGenerator.withEmptyDb()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `requests GET - admin can read all fields`() {
        // given
        testDataGenerator
            .withEmptyDb()
            .withRequest()
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].hasHiddenFields").value(false))
    }

    @Test
    @WithMockUser(username = "bar")
    fun `request GET - with hidden fields`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .getRequest().value()

        // when / then
        this.mockMvc.perform(get("/requests/{id}", request.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(request.id.toString()))
            .andExpect(jsonPath("createdAt").value(request.createdAt.toString()))
            .andExpect(jsonPath("createdBy").value(request.createdBy))
            .andExpect(jsonPath("hasHiddenFields").value(true))
            .andExpect(jsonPath("updatedAt").value(request.updatedAt.toString()))
            .andExpect(jsonPath("updatedBy").value(request.updatedBy))
    }

    @Test
    @WithMockUser(username = "bar")
    fun `requests GET - with some hidden fields`() {
        // given
        testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .withRequest("bar")

        // when / then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[?(@.createdBy=='foo')].hasHiddenFields").value(true))
            .andExpect(jsonPath("$[?(@.createdBy=='bar')].hasHiddenFields").value(false))
    }

    @Test
    @WithMockUser(username = "foo")
    fun `request DELETE - success`() {
        // given
        val request = testDataGenerator
            .withEmptyDb()
            .withRequest("foo")
            .getRequest().value()

        // when
        this.mockMvc.perform(delete("/requests/{id}", request.id))
            .andExpect(status().isNoContent)

        // then
        this.mockMvc.perform(get("/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `requests POST - create request success`() {
        // given
        testDataGenerator.withEmptyDb()
        val request: TestData<RequestDto> = testDataGenerator.random()
        val start = Instant.now()

        // when
        val response = this.mockMvc
            .perform(
                MockMvcRequestBuilders.post("/requests")
                    .content(request.toJson())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON.toString()))
            .andReturn()
        val returnedRequest = toObject<RequestDto>(response)

        // then
        assertThat(requestRepository.count()).isEqualTo(1)
        assertThat(returnedRequest.id).isNotNull
        assertThat(returnedRequest.ownerName).isEqualTo(Users.VALID_USER_NAME)
        assertThat(returnedRequest.createdBy).isEqualTo(VALID_USER_ID)
        assertThat(returnedRequest.createdAt).isBetween(start, Instant.now())
        assertThat(returnedRequest.updatedAt).isEqualTo(returnedRequest.createdAt)
        assertThat(returnedRequest.updatedBy).isEqualTo(returnedRequest.createdBy)
    }

    @Test
    @WithMockUser
    fun `requests POST - body validation`() {
        // 200
        assert(status().isOk, "mileage", 0L)

        // 400
        assert(status().isBadRequest, "brand")
        assert(status().isBadRequest, "capacity", -100L)
        assert(status().isBadRequest, "horsePower", -100L)
        assert(status().isBadRequest, "mileage", -100L)
        assert(status().isBadRequest, "modelSeries")
    }

    fun assert(httpStatus: ResultMatcher, attribute: String, value: Any? = null) {
        val request: TestData<RequestDto> = testDataGenerator.random()
        request.setValue(attribute, value)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.post("/requests")
                    .content(request.toJson())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(httpStatus)
    }

}
