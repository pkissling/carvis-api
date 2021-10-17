package cloud.carvis.backend.util

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractApplicationTest {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var testDataGenerator: TestDataGenerator

    @Autowired
    lateinit var amazonDynamoDB: AmazonDynamoDB

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    companion object {

        @Container
        val dynamoDb = GenericContainer<Nothing>("amazon/dynamodb-local").apply {
            withExposedPorts(8000)
            start()
        }


        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("application.dynamodb.endpoint.ip", dynamoDb::getContainerIpAddress)
            registry.add("application.dynamodb.endpoint.port", dynamoDb::getFirstMappedPort)
        }
    }

    @BeforeAll
    fun setup() {
        setupDynamodb()
    }

    private fun setupDynamodb() {
        amazonDynamoDB.createTable(
            CreateTableRequest()
                .withTableName("carvis-dev-cars")
                .withProvisionedThroughput(ProvisionedThroughput(20, 20))
                .withAttributeDefinitions(
                    AttributeDefinition()
                        .withAttributeName("id")
                        .withAttributeType(S)
                )
                .withKeySchema(
                    KeySchemaElement()
                        .withAttributeName("id")
                        .withKeyType(KeyType.HASH)
                )
        )
    }

    @AfterAll
    fun tearDown() {
        dynamoDb.stop()
    }


}