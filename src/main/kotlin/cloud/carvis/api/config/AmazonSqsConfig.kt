package cloud.carvis.api.config

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.messaging.config.QueueMessageHandlerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver


@Configuration
class AmazonSqsConfig {

    // must match bean name exactly to make aws autoconfig back off
    @Bean("amazonSQS")
    fun amazonSqs(@Value("\${aws.region}") region: String): AmazonSQSAsync =
        AmazonSQSAsyncClientBuilder.standard()
            .withRegion(region)
            .build()

    @Bean
    fun queueMessageHandlerFactory(objectMapper: ObjectMapper): QueueMessageHandlerFactory {
        val messageConverter = MappingJackson2MessageConverter().apply {
            isStrictContentTypeMatch = false
            setObjectMapper(objectMapper)
        }
        return QueueMessageHandlerFactory().apply {
            setArgumentResolvers(listOf(PayloadMethodArgumentResolver(messageConverter)))
        }
    }
}
