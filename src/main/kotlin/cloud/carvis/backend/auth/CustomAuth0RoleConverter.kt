package cloud.carvis.backend.auth

import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomAuth0RoleConverter(@Value("\${auth.custom-role-claim-key}") val claimKey: String): Converter<Jwt, JwtAuthenticationToken> {

    override fun convert(source: Jwt): JwtAuthenticationToken = source.claims[claimKey].let { customRoles ->
        if (customRoles == null || customRoles !is JSONArray) {
            return JwtAuthenticationToken(source)
        }

        val authorities = customRoles
            .map { it.toString() }
            .map { ROLE_PREFIX + it.uppercase(Locale.getDefault()) }
            .map { OAuth2UserAuthority(it, DUMMY_PERMISSION) }

        return JwtAuthenticationToken(source, authorities)
    }

    companion object {
        val DUMMY_PERMISSION = mapOf("dummy_key" to "dummy_value")
        const val ROLE_PREFIX = "ROLE_"
    }

}