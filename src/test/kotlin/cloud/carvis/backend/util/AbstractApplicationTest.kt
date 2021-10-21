package cloud.carvis.backend.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc


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

}