package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_NAME
import cloud.carvis.api.dao.repositories.RequestRepository
import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.util.testdata.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant.now
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
            .andExpect(jsonPath("contactDetails.company").value(request.contactCompany))
            .andExpect(jsonPath("contactDetails.email").value(request.contactEmail))
            .andExpect(jsonPath("contactDetails.freeText").value(request.contactFreeText))
            .andExpect(jsonPath("contactDetails.name").value(request.contactName))
            .andExpect(jsonPath("contactDetails.phone").value(request.contactPhone))
            .andExpect(jsonPath("condition").value(request.condition))
            .andExpect(jsonPath("description").value(request.description))
            .andExpect(jsonPath("hasHiddenFields").value(false))
            .andExpect(jsonPath("highlights").value(request.highlights))
            .andExpect(jsonPath("horsePower").value(request.horsePower))
            .andExpect(jsonPath("mileage").value(request.mileage))
            .andExpect(jsonPath("mustHaves").value(request.mustHaves))
            .andExpect(jsonPath("noGos").value(request.noGos))
            .andExpect(jsonPath("modelDetails").value(request.modelDetails))
            .andExpect(jsonPath("modelSeries").value(request.modelSeries))
            .andExpect(jsonPath("modelYear").value(request.modelYear))
            .andExpect(jsonPath("ownerName").value(VALID_USER_NAME))
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
            .andExpect(jsonPath("$[0].contactDetails.name").exists())
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
            .andExpect(jsonPath("contactDetails").doesNotExist())
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
            .andExpect(jsonPath("$[?(@.createdBy=='foo')].contactDetails.name").doesNotExist())
            .andExpect(jsonPath("$[?(@.createdBy=='bar')].hasHiddenFields").value(false))
            .andExpect(jsonPath("$[?(@.createdBy=='bar')].contactDetails.name").exists())
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
        val start = now()

        // when
        val response = this.mockMvc
            .perform(
                post("/requests")
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
        assertThat(returnedRequest.ownerName).isEqualTo(VALID_USER_NAME)
        assertThat(returnedRequest.createdBy).isEqualTo(VALID_USER_ID)
        assertThat(returnedRequest.createdAt).isBetween(start, now())
        assertThat(returnedRequest.updatedAt).isEqualTo(returnedRequest.createdAt)
        assertThat(returnedRequest.updatedBy).isEqualTo(returnedRequest.createdBy)
    }

    @Test
    @WithMockUser
    fun `requests POST - body validation`() {
        // 200
        assert("mileage", 0L).andExpect(status().isOk)

        // 400
        assert("brand").andExpect(status().isBadRequest)
        assert("capacity", -100L).andExpect(status().isBadRequest)
        assert("horsePower", -100L).andExpect(status().isBadRequest)
        assert("mileage", -100L).andExpect(status().isBadRequest)
        assert("type").andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser("foo")
    fun `request PUT - modify other users request forbidden`() {
        // given
        val request = testDataGenerator
            .withRequest("bar")
            .getRequest()

        // when / then
        this.mockMvc.perform(
            put("/requests/{id}", request.value().id)
                .content(testDataGenerator.random<RequestDto>().toJson())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "foo", roles = ["ADMIN"])
    fun `request PUT - admin can modify other users request`() {
        // given
        val start = now()
        val request = testDataGenerator
            .withRequest("bar")
            .getRequest().value()

        // when / then
        this.mockMvc.perform(
            put("/requests/{id}", request.id)
                .content(testDataGenerator.random<RequestDto>().toJson())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.createdBy").value("bar"))

        // then
        val updatedRequest = requestRepository.findByIdOrNull(request.id!!)!!
        assertThat(updatedRequest.createdBy).isEqualTo("bar")
        assertThat(updatedRequest.createdAt).isEqualTo(request.createdAt)
        assertThat(updatedRequest.updatedBy).isEqualTo("foo")
        assertThat(updatedRequest.updatedAt).isBetween(start, now())
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `request PUT - update existing request`() {
        // given
        val start = now()
        val existingRequest = testDataGenerator
            .withEmptyDb()
            .withRequest(VALID_USER_ID)
            .getRequest()
            .value()
        val requestId = existingRequest.id.toString()
        val request: TestData<RequestDto> = testDataGenerator.random()

        // when
        val response = this.mockMvc
            .perform(
                put("/requests/{id}", requestId)
                    .content(request.toJson())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(requestId))
            .andExpect(jsonPath("createdAt").value(existingRequest.createdAt.toString()))
            .andExpect(jsonPath("createdBy").value(VALID_USER_ID))
            .andExpect(jsonPath("updatedBy").value(VALID_USER_ID))
            .andExpect(jsonPath("additionalEquipment").value(request.value().additionalEquipment))
            .andExpect(jsonPath("bodyType").value(request.value().bodyType))
            .andExpect(jsonPath("brand").value(request.value().brand))
            .andExpect(jsonPath("budget").value(request.value().budget))
            .andExpect(jsonPath("capacity").value(request.value().capacity))
            .andExpect(jsonPath("countryOfOrigin").value(request.value().countryOfOrigin))
            .andExpect(jsonPath("colorExterior").value(request.value().colorExterior))
            .andExpect(jsonPath("colorExteriorManufacturer").value(request.value().colorExteriorManufacturer))
            .andExpect(jsonPath("colorAndMaterialInterior").value(request.value().colorAndMaterialInterior))
            .andExpect(jsonPath("condition").value(request.value().condition))
            .andExpect(jsonPath("contactDetails.company").value(request.value().contactDetails!!.company))
            .andExpect(jsonPath("contactDetails.email").value(request.value().contactDetails!!.email))
            .andExpect(jsonPath("contactDetails.freeText").value(request.value().contactDetails!!.freeText))
            .andExpect(jsonPath("contactDetails.name").value(request.value().contactDetails!!.name))
            .andExpect(jsonPath("contactDetails.phone").value(request.value().contactDetails!!.phone))
            .andExpect(jsonPath("description").value(request.value().description))
            .andExpect(jsonPath("hasHiddenFields").value(false))
            .andExpect(jsonPath("highlights").value(request.value().highlights))
            .andExpect(jsonPath("horsePower").value(request.value().horsePower))
            .andExpect(jsonPath("mileage").value(request.value().mileage))
            .andExpect(jsonPath("mustHaves").value(request.value().mustHaves))
            .andExpect(jsonPath("noGos").value(request.value().noGos))
            .andExpect(jsonPath("modelDetails").value(request.value().modelDetails))
            .andExpect(jsonPath("modelSeries").value(request.value().modelSeries))
            .andExpect(jsonPath("modelYear").value(request.value().modelYear))
            .andExpect(jsonPath("ownerName").value(VALID_USER_NAME))
            .andExpect(jsonPath("transmission").value(request.value().transmission))
            .andExpect(jsonPath("type").value(request.value().type))
            .andExpect(jsonPath("vision").value(request.value().vision))
            .andReturn()
        val returnedRequest = toObject<RequestDto>(response)

        // then
        assertThat(returnedRequest.updatedAt).isBetween(start, now())
    }

    fun assert(attribute: String, value: Any? = null): ResultActions {
        val request: TestData<RequestDto> = testDataGenerator.random()
        request.setValue(attribute, value)

        return this.mockMvc
            .perform(
                post("/requests")
                    .content(request.toJson())
                    .contentType(MediaType.APPLICATION_JSON)
            )
    }
}
