package cloud.carvis.api.common.events.model

import java.time.Instant

data class UserSignupEvent(
    val userId: String,
    val email: String,
    val name: String? = null,
    val createdAt: Instant? = null
)
