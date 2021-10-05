package cloud.carvis.backend.util

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractApplicationTest {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var testDataGenerator: TestDataGenerator

    @Autowired
    lateinit var amazonDynamoDB: AmazonDynamoDB

    companion object {

        lateinit var requestSpecification: RequestSpecification

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
        setupRestAssured()
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

    private fun setupRestAssured() {
        val logConfig = LogConfig.logConfig()
            .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
        val config = RestAssuredConfig.config().logConfig(logConfig)

        requestSpecification = RequestSpecBuilder()
            .setBaseUri("http://localhost:${port}")
            .setBasePath("/")
            .setContentType(ContentType.JSON)
            .setRelaxedHTTPSValidation()
            .setConfig(config)
            .build()
    }

    @AfterAll
    fun tearDown() {
        RestAssured.reset()
        dynamoDb.stop()
    }


}