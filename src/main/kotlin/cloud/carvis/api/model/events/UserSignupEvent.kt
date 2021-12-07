package cloud.carvis.api.model.events

import java.time.Instant

data class UserSignupEvent(
    val userId: String,
    val username: String,
    val name: String,
    val email: String,
    val createdAt: Instant
)
