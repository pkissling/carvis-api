package cloud.carvis.backend.testconfig

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.time.Instant

@TestConfiguration
class JwtDecoderTestConfig {

    // dummy jwt decoder to prevent actual oauth2 config to resolve meta info from auth0 over the internet
    @Bean
    fun jwtDecoder(): JwtDecoder {
        return JwtDecoder { token ->
            Jwt(token, Instant.now(), Instant.now(), emptyMap(), emptyMap())
        }
    }
}
