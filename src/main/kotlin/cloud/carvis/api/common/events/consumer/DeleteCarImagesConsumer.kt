package cloud.carvis.api.common.events.consumer

import cloud.carvis.api.common.events.model.CarDeletedEvent
import cloud.carvis.api.common.events.model.CarvisEvent
import cloud.carvis.api.images.service.ImageService
import org.springframework.stereotype.Service

@Service
class DeleteCarImagesConsumer(
    private val imageService: ImageService
) : CarvisEventConsumer<CarDeletedEvent> {

    override fun canConsume(event: CarvisEvent): Boolean =
        event is CarDeletedEvent

    override fun consume(event: CarDeletedEvent) =
        imageService.deleteImages(event.imageIds)

}
