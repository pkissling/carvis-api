package cloud.carvis.backend.config

import cloud.carvis.backend.properties.CorsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurationSource(corsProperties: CorsProperties): CorsConfigurationSource =
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                    corsProperties.allowedOrigins.forEach { addAllowedOrigin(it) }
                    corsProperties.allowedMethods.forEach { addAllowedMethod(it) }
                }
            )
        }
}
