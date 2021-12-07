package cloud.carvis.api.config

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AmazonS3Config {

    @Bean
    fun amazonS3(@Value("\${aws.region}") region: String): AmazonS3 =
        AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .build()
}
