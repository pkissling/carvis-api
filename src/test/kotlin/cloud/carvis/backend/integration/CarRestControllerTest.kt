package cloud.carvis.backend.integration

import cloud.carvis.backend.util.AbstractApplicationTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test


class CarRestControllerTest : AbstractApplicationTest() {

    @Test
    fun `cars GET - no cars`() {
        Given {
            spec(requestSpecification)
        } When {
            get("/cars")
        } Then {
            statusCode(200)
            body(equalTo("[]"))
        }
    }
}
