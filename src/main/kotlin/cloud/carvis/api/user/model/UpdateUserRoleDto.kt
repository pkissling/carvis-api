package cloud.carvis.api.user.model

data class UpdateUserRoleDto(
    var addRoles: List<String> = emptyList(),

    var removeRoles: List<String> = emptyList(),
)
