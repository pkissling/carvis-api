package cloud.carvis.backend.util

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName


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
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractApplicationTest {

    @Autowired
    lateinit var testDataGenerator: TestDataGenerator

    @Autowired
    protected lateinit var mockMvc: MockMvc

    companion object {

        @JvmStatic
        @Container
        val dynamoDb = GenericContainer<Nothing>("amazon/dynamodb-local")
            .apply {
                withExposedPorts(8000)
                start()
            }

        @JvmStatic
        @Container
        val s3 = LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.19"))
            .withServices(S3)
            .apply {
                this.start()
            }


        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("application.dynamodb.endpoint.ip", dynamoDb::getContainerIpAddress)
            registry.add("application.dynamodb.endpoint.port", dynamoDb::getFirstMappedPort)
            registry.add("application.s3.endpoint.port", s3::getFirstMappedPort)
        }
    }

    @AfterAll
    fun tearDown() {
        dynamoDb.stop()
    }
}