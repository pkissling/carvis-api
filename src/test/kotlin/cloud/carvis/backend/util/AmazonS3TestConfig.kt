package cloud.carvis.backend.util

import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class AmazonS3TestConfig {

    @Bean
    fun amazonS3(
        @Value("\${application.s3.endpoint.port}") port: String
    ): AmazonS3 = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://127.0.0.1:${port}", "eu-west-1"))
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("fake", "fake")))
        .build()

    @Autowired
    fun createBuckets(amazonS3: AmazonS3, s3Properties: S3Properties) =
        s3Properties.bucketNames.values.forEach {
            amazonS3.createBucket(it)
        }
}