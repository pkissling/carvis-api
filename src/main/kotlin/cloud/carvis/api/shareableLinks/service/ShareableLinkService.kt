package cloud.carvis.api.shareableLinks.service

import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.cars.service.CarService
import cloud.carvis.api.common.events.publisher.CarvisEventPublisher
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import cloud.carvis.api.shareableLinks.mapper.ShareableLinkMapper
import cloud.carvis.api.shareableLinks.model.CreateShareableLinkRequestDto
import cloud.carvis.api.shareableLinks.model.ShareableLinkDto
import cloud.carvis.api.shareableLinks.model.ShareableLinkEntity
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
    private val carvisEventPublisher: CarvisEventPublisher,
    private val mapper: ShareableLinkMapper
) {

    private val logger = KotlinLogging.logger {}

    fun createShareableLink(carId: UUID, request: CreateShareableLinkRequestDto): ShareableLinkDto {
        if (carService.fetchCar(carId) == null) {
            throw ResponseStatusException(NOT_FOUND, "car not found")
        }

        val entity = ShareableLinkEntity(
            carId = carId,
            recipientName = request.recipientName,
            visitorCount = AtomicLong(0),
        )

        shareableLinkRepository.save(entity)

        return entity.shareableLinkReference
            ?.let { shareableLinkRepository.findByHashKey(it) }
            ?.let { mapper.toDto(it) }
            ?: throw RuntimeException("Unable to read saved shared link entity")
    }

    fun fetchCarFromShareableLinkReference(shareableLinkReference: String): CarDto {
        val carId = shareableLinkRepository.findByHashKey(shareableLinkReference)
            ?.carId
            ?: throw ResponseStatusException(NOT_FOUND, "shareable link not found")

        return carService.fetchCar(carId)
            .also { carvisEventPublisher.shareableLinkVisited(shareableLinkReference) }
            ?: throw ResponseStatusException(BAD_REQUEST, "car not found")
    }

    fun increaseVisitorCount(shareableLinkReference: String) {
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

    fun fetchShareableLinks(): List<ShareableLinkDto> =
        shareableLinkRepository.findAll()
            .map { mapper.toDto(it) }
            .toList()

    fun deleteShareableLink(shareableLinkReference: String) {
        val exists = shareableLinkRepository.existsByHashKey(shareableLinkReference)
        if (!exists) {
            throw ResponseStatusException(NOT_FOUND, "shareable link not found")
        }
        shareableLinkRepository.deleteByHashKey(shareableLinkReference)
    }

    fun findByCarId(carId: UUID): List<ShareableLinkEntity> =
        shareableLinkRepository.findByCarId(carId)

    fun delete(vararg shareableLinkEntities: ShareableLinkEntity) =
        shareableLinkRepository.delete(*shareableLinkEntities)

    fun shareableLinksCount(): Int =
        shareableLinkRepository.count()
}
