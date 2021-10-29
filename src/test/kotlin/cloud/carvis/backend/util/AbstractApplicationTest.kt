package cloud.carvis.backend.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tyro.oss.arbitrater.arbitrater
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import kotlin.reflect.KClass


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.data.dynamodb.entity2ddl.auto=create-only",
        "spring.main.allow-bean-definition-overriding=true"
    ],
    classes = [
        AmazonS3TestConfig::class,
        AmazonDynamoDbTestConfig::class
    ]
)
@AutoConfigureMockMvc
abstract class AbstractApplicationTest {

    @Autowired
    lateinit var testDataGenerator: TestDataGenerator

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected fun <T : Any> random(clazz: KClass<T>): TestData<T> {
        val value = clazz.arbitrater()
            .generateNulls(false)
            .useDefaultValues(false)
            .createInstance()
        return TestData(objectMapper, value)
    }

    protected final inline fun <reified T : Any> toObject(r: MockHttpServletResponse): T {
        return objectMapper.readValue<T>(r.contentAsByteArray)
    }
}
