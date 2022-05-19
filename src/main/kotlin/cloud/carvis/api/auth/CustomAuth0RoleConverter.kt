package cloud.carvis.api.auth

import cloud.carvis.api.properties.AudienceType.SYSTEM
import cloud.carvis.api.properties.AuthProperties
import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomAuth0RoleConverter(
    @Value("\${auth.custom-role-claim-key}") private val claimKey: String,
    authProperties: AuthProperties
) : Converter<Jwt, JwtAuthenticationToken> {

    val systemAudience = authProperties.audiences[SYSTEM] ?: throw RuntimeException("SYSTEM audience not defined")

    override fun convert(source: Jwt): JwtAuthenticationToken {
        val customRoles = source.claims[claimKey]
        if (customRoles != null && customRoles is JSONArray) {
            val authorities = customRoles
                .map { it.toString() }
                .map { ROLE_PREFIX + it.uppercase(Locale.getDefault()) }
                .map { GrantedAuthority { it } }

            return JwtAuthenticationToken(source, authorities)
        }

        val audience = source.claims["aud"]
        if (isSystemUser(audience)) {
            return JwtAuthenticationToken(source, listOf(GrantedAuthority { "${ROLE_PREFIX}SYSTEM" }))
        }
        return JwtAuthenticationToken(source)
    }

    private fun isSystemUser(audiences: Any?): Boolean {
        return audiences is ArrayList<*> && audiences.contains(systemAudience)
    }

    companion object {
        const val ROLE_PREFIX = "ROLE_"
    }
}
