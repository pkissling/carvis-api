package cloud.carvis.api.mapper

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.model.entities.RequestEntity
import cloud.carvis.api.service.AuthorizationService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RequestMapper(
    private val authorizationService: AuthorizationService
) : Mapper<RequestDto, RequestEntity> {

    private val logger = KotlinLogging.logger {}

    override fun toDto(entity: RequestEntity): RequestDto =
        RequestDto(
            id = entity.id,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            hasHiddenFields = false,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy
        )
            .let { hideFieldsIfRequired(it) }

    override fun toEntity(dto: RequestDto): RequestEntity =
        RequestEntity()

    private fun hideFieldsIfRequired(dto: RequestDto): RequestDto {
        val isAdmin = authorizationService.isAdmin()
        val username = authorizationService.getUsername()

        if (isAdmin || username == dto.createdBy) {
            return dto
        }

        logger.debug {
            "Hiding fields for request with id [${dto.id}]. " +
                    "Owner username [${dto.createdBy}, requester username [$username]"
        }

        return dto.apply {
            hasHiddenFields = true
        }
    }
}
