package cloud.carvis.api.cars.mapper

import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.cars.model.CarEntity
import cloud.carvis.api.common.dao.mapper.EntityMapper
import cloud.carvis.api.users.service.UserService
import org.springframework.stereotype.Service

@Service
class CarMapper(private val userService: UserService) : EntityMapper<CarDto, CarEntity> {

    override fun toDto(entity: CarEntity): CarDto =
        CarDto(
            id = entity.id,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
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
            description = entity.description,
            horsePower = entity.horsePower,
            images = entity.images,
            mileage = entity.mileage,
            modelDetails = entity.modelDetails,
            modelSeries = entity.modelSeries,
            modelYear = entity.modelYear,
            ownerName = entity.createdBy?.let { userService.fetchUserSafe(it)?.name } ?: entity.createdBy,
            price = entity.price,
            shortDescription = entity.shortDescription,
            transmission = entity.transmission,
            type = entity.type,
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
