package cloud.carvis.backend.util

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class TestData<T>(
    private val objectMapper: ObjectMapper,
    private val o: T
) {

    fun toJson(): ByteArray = objectMapper.writeValueAsBytes(o)

    fun value(): T = o

    fun setValue(attribute: String, value: Any?) {
        val property = o!!::class.memberProperties.first { it.name == attribute } as KMutableProperty<*>
        property.setter.call(o, value)
    }
}
