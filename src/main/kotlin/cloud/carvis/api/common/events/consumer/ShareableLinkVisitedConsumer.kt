package cloud.carvis.api.common.events.consumer

import cloud.carvis.api.common.events.model.CarvisEvent
import cloud.carvis.api.common.events.model.ShareableLinkVisited
import cloud.carvis.api.shareableLinks.service.ShareableLinkService
import org.springframework.stereotype.Service

@Service
class ShareableLinkVisitedConsumer(
    private val shareableLinkService: ShareableLinkService
) : CarvisEventConsumer<ShareableLinkVisited> {

    override fun canConsume(event: CarvisEvent): Boolean =
        event is ShareableLinkVisited

    override fun consume(event: ShareableLinkVisited) {
        shareableLinkService.increaseVisitorCount(event.shareableLinkReference)
    }
}
