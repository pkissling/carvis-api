package cloud.carvis.backend.mapper

import cloud.carvis.backend.model.dtos.CarDto
import cloud.carvis.backend.model.entities.CarEntity
import cloud.carvis.backend.service.ImageService
import org.springframework.stereotype.Service

@Service
class CarMapper(
    private val imageService: ImageService
) {

    fun fromEntity(entity: CarEntity): CarDto =
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
            images = imageService.resolveImageUrls(entity.images),
            mileage = entity.mileage,
            modelDetails = entity.modelDetails,
            modelSeries = entity.modelSeries,
            modelYear = entity.modelYear,
            ownerName = entity.ownerName,
            ownerUsername = entity.ownerUsername,
            price = entity.price,
            transmission = entity.transmission,
            type = entity.type,
            updatedAt = entity.updatedAt,
            vin = entity.vin
        )
}