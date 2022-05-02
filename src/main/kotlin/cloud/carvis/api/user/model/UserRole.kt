package cloud.carvis.api.user.model

import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

enum class UserRole {
    ADMIN, USER;

    @JsonValue
    open fun toJsonValue(): String {
        return this.name.lowercase(Locale.getDefault())
    }

    companion object {
        fun from(from: String): UserRole = valueOf(from.uppercase(Locale.getDefault()))
    }
}
