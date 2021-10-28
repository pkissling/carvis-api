package cloud.carvis.backend.util

class TestData<T>(
    private val toJson: (T) -> ByteArray,
    private val value: T
) {

    fun toJson(): ByteArray = toJson.invoke(value)

    fun value(): T = value
}
