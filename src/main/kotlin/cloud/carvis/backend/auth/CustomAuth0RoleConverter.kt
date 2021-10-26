package cloud.carvis.backend.auth

import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomAuth0RoleConverter(@Value("\${auth.custom-role-claim-key}") val claimKey: String): Converter<Jwt, JwtAuthenticationToken> {

    override fun convert(source: Jwt): JwtAuthenticationToken {
        val customRoles = source.claims[claimKey]
        if (customRoles == null || customRoles !is JSONArray) {
            return JwtAuthenticationToken(source)
        }

        val authorities = customRoles
            .map { it.toString() }
            .map { ROLE_PREFIX + it.uppercase(Locale.getDefault()) }
            .map { GrantedAuthority { it } }

        return JwtAuthenticationToken(source, authorities)
    }

    companion object {
        const val ROLE_PREFIX = "ROLE_"
    }
}