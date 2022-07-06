package cloud.carvis.api.common.events.consumer

import cloud.carvis.api.common.events.model.CarvisEvent

interface CarvisEventConsumer<T : CarvisEvent> {

    fun canConsume(event: CarvisEvent): Boolean

    fun consume(event: T)

}
