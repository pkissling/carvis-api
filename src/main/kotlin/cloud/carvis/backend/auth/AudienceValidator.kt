package cloud.carvis.backend.auth

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {

    override fun validate(jwt: Jwt?): OAuth2TokenValidatorResult {
        if (jwt == null) {
            return invalidToken()
        }
        val audiences: List<String> = jwt.audience
        if (audiences.contains(audience)) {
            return OAuth2TokenValidatorResult.success()
        }
        return invalidToken()
    }

    private fun invalidToken(): OAuth2TokenValidatorResult {
        val err = OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN)
        return OAuth2TokenValidatorResult.failure(err)
    }
}