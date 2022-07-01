package cloud.carvis.api.requests.mapper

import cloud.carvis.api.common.auth.service.AuthorizationService
import cloud.carvis.api.common.dao.mapper.EntityMapper
import cloud.carvis.api.model.dtos.ContactDetailsDto
import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.requests.model.entities.RequestEntity
import cloud.carvis.api.users.service.UserService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class RequestMapper(
    private val authorizationService: AuthorizationService,
    private val userService: UserService
) : EntityMapper<UUID, RequestDto, RequestEntity> {

    private val logger = KotlinLogging.logger {}

    override fun toDto(entity: RequestEntity): RequestDto =
        RequestDto(
            id = entity.id,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
            additionalEquipment = entity.additionalEquipment,
            bodyType = entity.bodyType,
            brand = entity.brand,
            budget = entity.budget,
            capacity = entity.capacity,
            countryOfOrigin = entity.countryOfOrigin,
            colorExterior = entity.colorExterior,
            colorExteriorManufacturer = entity.colorExteriorManufacturer,
            colorAndMaterialInterior = entity.colorAndMaterialInterior,
            contactDetails = ContactDetailsDto(
                company = entity.contactCompany,
                email = entity.contactEmail,
                freeText = entity.contactFreeText,
                name = entity.contactName,
                phone = entity.contactPhone,
            ),
            condition = entity.condition,
            description = entity.description,
            hasHiddenFields = false,
            highlights = entity.highlights,
            horsePower = entity.horsePower,
            mileage = entity.mileage,
            modelDetails = entity.modelDetails,
            modelSeries = entity.modelSeries,
            modelYear = entity.modelYear,
            mustHaves = entity.mustHaves,
            noGos = entity.noGos,
            ownerName = entity.createdBy?.let { userService.fetchUserSafe(it)?.name } ?: entity.createdBy,
            transmission = entity.transmission,
            type = entity.type,
            vision = entity.vision,
        )
            .let { hideFieldsIfRequired(it) }

    override fun toEntity(dto: RequestDto): RequestEntity =
        RequestEntity(
            additionalEquipment = dto.additionalEquipment,
            bodyType = dto.bodyType,
            brand = dto.brand,
            budget = dto.budget,
            capacity = dto.capacity,
            countryOfOrigin = dto.countryOfOrigin,
            colorExterior = dto.colorExterior,
            colorExteriorManufacturer = dto.colorExteriorManufacturer,
            colorAndMaterialInterior = dto.colorAndMaterialInterior,
            condition = dto.condition,
            contactCompany = dto.contactDetails?.company,
            contactEmail = dto.contactDetails?.email,
            contactFreeText = dto.contactDetails?.freeText,
            contactName = dto.contactDetails?.name,
            contactPhone = dto.contactDetails?.phone,
            description = dto.description,
            highlights = dto.highlights,
            horsePower = dto.horsePower,
            mileage = dto.mileage,
            modelDetails = dto.modelDetails,
            modelSeries = dto.modelSeries,
            modelYear = dto.modelYear,
            mustHaves = dto.mustHaves,
            noGos = dto.noGos,
            ownerName = dto.ownerName,
            transmission = dto.transmission,
            type = dto.type,
            vision = dto.vision,
        )

    private fun hideFieldsIfRequired(dto: RequestDto): RequestDto {
        val isAdmin = authorizationService.isAdmin()
        val userId = authorizationService.getUserId()

        if (isAdmin || userId == dto.createdBy) {
            return dto
        }

        logger.debug {
            "Hiding fields for request with id [${dto.id}]. " +
                    "Owner userId [${dto.createdBy}, requester userId [$userId]"
        }

        return dto.apply {
            hasHiddenFields = true
            contactDetails = null
        }
    }
}
