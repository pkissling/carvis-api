package cloud.carvis.api.user.mapper

import cloud.carvis.api.mapper.Mapper
import cloud.carvis.api.user.model.UserDto
import com.auth0.json.mgmt.users.User
import org.springframework.stereotype.Service

@Service
class UserMapper() : Mapper<UserDto, User> {

    override fun toDto(entity: User): UserDto =
        UserDto(
            userId = entity.id,
            name = entity.name,
            company = entity.userMetadata?.get("company")?.toString(),
        )


    override fun toEntity(dto: UserDto): User =
        User().apply {
            name = dto.name
            userMetadata = mapOf("company" to dto.company)
        }
}
