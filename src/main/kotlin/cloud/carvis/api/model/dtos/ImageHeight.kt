package cloud.carvis.api.model.dtos

enum class ImageHeight {
    ORIGINAL,
    `48`,
    `100`,
    `200`,
    `500`,
    `1080`;

    fun toInt(): Int =
        try {
            this.name.toInt()
        } catch (e: Error) {
            throw RuntimeException("ImageHeight [$this] can not be converted to Integer", e)
        }
}
