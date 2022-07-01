package cloud.carvis.api.users.mapper

import cloud.carvis.api.common.clients.UserWithRoles
import cloud.carvis.api.common.mapper.Mapper
import cloud.carvis.api.users.dao.NewUserRepository
import cloud.carvis.api.users.model.UserDto
import com.auth0.json.mgmt.users.User
import org.springframework.stereotype.Service

@Service
class UserMapper(private val newUserRepository: NewUserRepository) : Mapper<UserDto, UserWithRoles> {

    override fun toDto(entity: UserWithRoles): UserDto =
        UserDto(
            userId = entity.user.id,
            name = entity.user.name,
            company = entity.user.userMetadata?.get("company")?.toString(),
            phone = entity.user.userMetadata?.get("phone")?.toString(),
            email = entity.user.email,
            roles = entity.roles,
            isNewUser = entity.user.id?.let { newUserRepository.existsByHashKey(it) } ?: false,
            picture = entity.user.picture
        )


    override fun toEntity(dto: UserDto): UserWithRoles =
        UserWithRoles(
            User().apply {
                name = dto.name
                userMetadata = mapOf(
                    "company" to dto.company,
                    "phone" to dto.phone
                )
            },
            dto.roles
        )
}
