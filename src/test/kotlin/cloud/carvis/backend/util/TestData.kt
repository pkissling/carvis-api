package cloud.carvis.backend.util

import com.fasterxml.jackson.databind.ObjectMapper

class TestData<T>(
    private val objectMapper: ObjectMapper,
    private val value: T
) {

    fun toJson(): ByteArray = objectMapper.writeValueAsBytes(value)

    fun value(): T = value
}
