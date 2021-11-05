package cloud.carvis.backend.util

import cloud.carvis.backend.restclients.Auth0RestClient
import cloud.carvis.backend.util.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.backend.util.AbstractApplicationTest.Users.VALID_USER_NAME
import com.auth0.json.mgmt.users.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.data.dynamodb.entity2ddl.auto=create-only",
        "spring.main.allow-bean-definition-overriding=true",
        "sentry.dsn=https://329f4264c94b452f8756d77a0c736606@o582664.ingest.sentry.io/6036448"
    ],
    classes = [
        AmazonS3TestConfig::class,
        AmazonDynamoDbTestConfig::class,
        JwtDecoderTestConfig::class,
        Auth0TestConfig::class
    ]
)
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
abstract class AbstractApplicationTest {

    @Autowired
    lateinit var testDataGenerator: TestDataGenerator

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockBean
    protected lateinit var auth0RestClient: Auth0RestClient

    @BeforeEach
    fun beforeAll() {
        resetMocks()
        mockAuth0()
    }

    private fun resetMocks() {
        reset(auth0RestClient)
    }

    private fun mockAuth0() {
        val user = User().apply {
            username = VALID_USER_NAME
        }
        doReturn(user).whenever(auth0RestClient).fetchUserDetails(VALID_USER_ID)
    }

    protected final inline fun <reified T : Any> toObject(r: MockHttpServletResponse): T {
        return objectMapper.readValue<T>(r.contentAsByteArray)
    }

    object Users {
        const val VALID_USER_ID = "a-random-user-id"
        const val VALID_USER_NAME = "Foo Bar"
    }
}
