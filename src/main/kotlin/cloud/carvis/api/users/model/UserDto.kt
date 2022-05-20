package cloud.carvis.api.users.model

import javax.validation.constraints.NotEmpty

data class UserDto(
    var userId: String? = null,

    @field:NotEmpty
    var name: String? = null,

    var company: String? = null,

    var phone: String? = null,

    var email: String? = null,

    var roles: List<UserRole> = emptyList(),

    var isNewUser: Boolean = false,

    var picture: String? = null

)
