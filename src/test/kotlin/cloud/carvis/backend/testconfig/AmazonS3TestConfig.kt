package cloud.carvis.backend.testconfig

import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class AmazonS3TestConfig {

    @Bean
    fun amazonS3(): AmazonS3 = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(s3.getEndpointConfiguration(Service.S3))
        .withCredentials(s3.defaultCredentialsProvider)
        .build()

    @Autowired
    fun createBuckets(amazonS3: AmazonS3, s3Properties: S3Properties) =
        s3Properties.bucketNames.values.forEach {
            amazonS3.createBucket(it)
        }

    companion object {

        @JvmStatic
        @Container
        val s3: LocalStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.19"))
            .withServices(Service.S3)
            .apply {
                this.start()
            }


        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("application.s3.endpoint.port", s3::getFirstMappedPort)
        }
    }
}
