package cloud.carvis.api.shareableLinks.mapper

import cloud.carvis.api.cars.service.CarService
import cloud.carvis.api.common.dao.mapper.EntityMapper
import cloud.carvis.api.shareableLinks.model.CarDetails
import cloud.carvis.api.shareableLinks.model.ShareableLinkDto
import cloud.carvis.api.shareableLinks.model.ShareableLinkEntity
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class ShareableLinkMapper(private val carService: CarService) :
    EntityMapper<String, ShareableLinkDto, ShareableLinkEntity> {

    override fun toDto(entity: ShareableLinkEntity): ShareableLinkDto =
        ShareableLinkDto(
            shareableLinkReference = entity.shareableLinkReference,
            carId = entity.carId,
            carDetails = entity.carId
                ?.let { carService.fetchCar(it) }
                ?.let {
                    CarDetails(
                        brand = it.brand,
                        type = it.type
                    )
                },
            visitorCount = entity.visitorCount?.get(),
            recipientName = entity.recipientName,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt
        )


    override fun toEntity(dto: ShareableLinkDto): ShareableLinkEntity =
        ShareableLinkEntity(
            carId = dto.carId,
            visitorCount = dto.visitorCount?.let { AtomicLong(it) },
            recipientName = dto.recipientName,
        )
}
