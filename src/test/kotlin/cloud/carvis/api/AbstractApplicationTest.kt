package cloud.carvis.api

import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_NAME
import cloud.carvis.api.user.model.UserDto
import cloud.carvis.api.util.mocks.Auth0Mock
import cloud.carvis.api.util.mocks.AwsMock
import cloud.carvis.api.util.testdata.TestDataGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult


@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "spring.data.dynamodb.entity2ddl.auto=create-only",
        "spring.main.allow-bean-definition-overriding=true",
        "sentry.dsn=https://329f4264c94b452f8756d77a0c736606@o582664.ingest.sentry.io/invalidprojectid"
    ],
    classes = [
        Auth0Mock::class,
        AwsMock::class
    ]
)
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
abstract class AbstractApplicationTest {

    @Autowired
    protected lateinit var testDataGenerator: TestDataGenerator

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var auth0Mock: Auth0Mock

    @AfterEach
    fun afterEach() {
        auth0Mock.reset()
    }

    protected final inline fun <reified T : Any> toObject(result: MvcResult): T {
        return objectMapper.readValue<T>(result.response.contentAsByteArray)
    }

    object Users {
        const val VALID_USER_ID = "a-random-user-id"
        const val VALID_USER_NAME = "Foo Bar"
    }
}
