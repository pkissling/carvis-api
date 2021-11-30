package cloud.carvis.api.mapper

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.model.entities.RequestEntity
import cloud.carvis.api.service.AuthorizationService
import cloud.carvis.api.service.UserService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RequestMapper(
    private val authorizationService: AuthorizationService,
    private val auth0RestClient: UserService
) : Mapper<RequestDto, RequestEntity> {

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
            ownerName = entity.createdBy?.let { auth0RestClient.fetchName(it) },
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
