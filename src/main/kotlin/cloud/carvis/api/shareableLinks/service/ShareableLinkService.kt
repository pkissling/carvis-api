package cloud.carvis.api.shareableLinks.service

import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.cars.service.CarService
import cloud.carvis.api.common.commands.publisher.CarvisCommandPublisher
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import cloud.carvis.api.shareableLinks.model.*
import mu.KotlinLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.concurrent.atomic.AtomicLong

@Service
class ShareableLinkService(
    private val shareableLinkRepository: ShareableLinkRepository,
    private val carService: CarService,
    private val carvisCommandPublisher: CarvisCommandPublisher
) {

    private val logger = KotlinLogging.logger {}

    fun createShareableLink(carId: UUID, request: CreateShareableLinkRequestDto): ShareableLinkDto {
        val car = carService.fetchCar(carId)
            ?: throw ResponseStatusException(NOT_FOUND, "car not found")
        val entity = ShareableLinkEntity(
            carId = carId,
            recipientName = request.recipientName,
            visitorCount = AtomicLong(0),
        )

        shareableLinkRepository.save(entity)

        val savedEntity = entity.shareableLinkReference
            ?.let { shareableLinkRepository.findByHashKey(it) }
            ?: throw RuntimeException("TODO")
        return ShareableLinkDto(
            shareableLinkReference = savedEntity.shareableLinkReference,
            carId = savedEntity.carId,
            carDetails = CarDetails(
                brand = car.brand,
                type = car.type
            ),
            visitorCount = savedEntity.visitorCount?.toLong(),
            recipientName = savedEntity.recipientName,
            createdAt = savedEntity.createdAt,
            createdBy = savedEntity.createdBy
        )
    }

    fun fetchCarFromShareableLinkReference(shareableLinkReference: ShareableLinkReference): CarDto {
        val carId = shareableLinkRepository.findByHashKey(shareableLinkReference)
            ?.carId
            ?: throw ResponseStatusException(NOT_FOUND, "shareable link not found")

        return carService.fetchCar(carId)
            .also { carvisCommandPublisher.increaseVisitorCounter(shareableLinkReference) }
            ?: throw ResponseStatusException(BAD_REQUEST, "car not found")
    }

    fun increaseVisitorCount(shareableLinkReference: ShareableLinkReference) {
        val reference = shareableLinkRepository.findByHashKey(shareableLinkReference)
            ?: throw RuntimeException("unable to find entity for shareable link reference: $shareableLinkReference")

        val visitorCount = reference.visitorCount
            ?: throw RuntimeException("visitorCount must not be null to shareable link reference: $shareableLinkReference")
        val expectedCounterValue = visitorCount.get().plus(1)

        visitorCount.incrementAndGet()
        shareableLinkRepository.save(reference)

        val actualCounterValue = shareableLinkRepository.findByHashKey(shareableLinkReference)
            ?.visitorCount
            ?.get()

        if (expectedCounterValue != actualCounterValue) {
            "Expected visitorCount of [$shareableLinkReference] to be [$expectedCounterValue] but was: $actualCounterValue"
                .also { logger.warn(it) }
                .also { throw RuntimeException(it) }
        }
        logger.info { "Increase counter to ${visitorCount.get()} for shareable link reference: $shareableLinkReference" }
    }
}
