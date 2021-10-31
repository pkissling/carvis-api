package cloud.carvis.backend.config

import cloud.carvis.backend.auth.AudienceValidator
import cloud.carvis.backend.auth.CustomAuth0RoleConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var customAuth0RoleConverter: CustomAuth0RoleConverter

    override fun configure(http: HttpSecurity?) {
        http!!
            .cors()
            .and()
            .csrf()
            .disable()
            .authorizeRequests()
            .mvcMatchers("/actuator/health").permitAll()
            .anyRequest().hasAnyRole("USER", "ADMIN")
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(customAuth0RoleConverter)
    }

    @Bean
    fun jwtDecoder(
        @Value("\${auth.audience}") audience: String,
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") issuer: String
    ): JwtDecoder {
        val withAudience: OAuth2TokenValidator<Jwt> = AudienceValidator(audience)
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuer)
        val validator: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withAudience, withIssuer)
        val jwtDecoder = JwtDecoders.fromOidcIssuerLocation<JwtDecoder>(issuer) as NimbusJwtDecoder
        jwtDecoder.setJwtValidator(validator)
        return jwtDecoder
    }
}
