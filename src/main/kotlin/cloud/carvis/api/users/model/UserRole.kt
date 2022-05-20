package cloud.carvis.api.users.model

import com.fasterxml.jackson.annotation.JsonValue
import mu.KotlinLogging
import org.springframework.security.core.GrantedAuthority
import java.util.*

enum class UserRole(private val isActualUser: Boolean) {
    ADMIN(true), USER(true), SYSTEM(false);

    fun isActualUser(): Boolean = isActualUser

    @JsonValue
    open fun toJsonValue(): String {
        return this.name.lowercase(Locale.getDefault())
    }

    fun isRole(s: String?): Boolean {
        return this.name == s?.uppercase()?.trim() || ROLE_PREFIX + this == s?.uppercase()?.trim()
    }

    fun toGrantedAuthority(): GrantedAuthority {
        return GrantedAuthority { ROLE_PREFIX + this.name }
    }

    companion object {
        const val ROLE_PREFIX = "ROLE_"

        private val logger = KotlinLogging.logger {}

        fun from(role: String): UserRole? = try {
            UserRole.valueOf(role.removePrefix(ROLE_PREFIX).uppercase())
        } catch (e: Exception) {
            logger.warn(e) { "Unable to convert given String to role: $role" }
            null
        }
    }
}
