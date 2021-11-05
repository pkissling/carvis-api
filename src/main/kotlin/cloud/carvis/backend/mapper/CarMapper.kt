package cloud.carvis.backend.mapper

import cloud.carvis.backend.model.dtos.CarDto
import cloud.carvis.backend.model.entities.CarEntity
import cloud.carvis.backend.service.UserService
import org.springframework.stereotype.Service

@Service
class CarMapper(private val auth0RestClient: UserService) : Mapper<CarDto, CarEntity> {

    override fun toDto(entity: CarEntity): CarDto =
        CarDto(
            id = entity.id,
            brand = entity.brand,
            bodyType = entity.bodyType,
            ads = entity.ads,
            additionalEquipment = entity.additionalEquipment,
            capacity = entity.capacity,
            colorAndMaterialInterior = entity.colorAndMaterialInterior,
            colorExterior = entity.colorExterior,
            colorExteriorManufacturer = entity.colorExteriorManufacturer,
            condition = entity.condition,
            countryOfOrigin = entity.countryOfOrigin,
            createdAt = entity.createdAt,
            description = entity.description,
            horsePower = entity.horsePower,
            images = entity.images,
            mileage = entity.mileage,
            modelDetails = entity.modelDetails,
            modelSeries = entity.modelSeries,
            modelYear = entity.modelYear,
            ownerName = entity.ownerUsername?.let { auth0RestClient.fetchUsername(it) },
            ownerUsername = entity.ownerUsername,
            price = entity.price,
            shortDescription = entity.shortDescription,
            transmission = entity.transmission,
            type = entity.type,
            updatedAt = entity.updatedAt,
            vin = entity.vin
        )

    override fun toEntity(dto: CarDto): CarEntity =
        CarEntity(
            brand = dto.brand,
            bodyType = dto.bodyType,
            ads = dto.ads,
            additionalEquipment = dto.additionalEquipment,
            capacity = dto.capacity,
            colorAndMaterialInterior = dto.colorAndMaterialInterior,
            colorExterior = dto.colorExterior,
            colorExteriorManufacturer = dto.colorExteriorManufacturer,
            condition = dto.condition,
            countryOfOrigin = dto.countryOfOrigin,
            description = dto.description,
            horsePower = dto.horsePower,
            images = dto.images,
            mileage = dto.mileage,
            modelDetails = dto.modelDetails,
            modelSeries = dto.modelSeries,
            modelYear = dto.modelYear,
            price = dto.price,
            shortDescription = dto.shortDescription,
            transmission = dto.transmission,
            type = dto.type,
            vin = dto.vin
        )
}
