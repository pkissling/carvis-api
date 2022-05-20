package cloud.carvis.api.common.auth.validator

import cloud.carvis.api.common.properties.AuthProperties
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(authProperties: AuthProperties) : OAuth2TokenValidator<Jwt> {

    private val configuredAudiences: Collection<String> = authProperties.audiences.values

    override fun validate(jwt: Jwt?): OAuth2TokenValidatorResult {
        if (jwt == null) {
            return invalidToken()
        }
        val jwtAudiences: List<String> = jwt.audience
        val isConfigured = jwtAudiences.all { configuredAudiences.contains(it) }
        if (isConfigured) {
            return OAuth2TokenValidatorResult.success()
        }
        return invalidToken()
    }

    private fun invalidToken(): OAuth2TokenValidatorResult {
        val err = OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN)
        return OAuth2TokenValidatorResult.failure(err)
    }
}
