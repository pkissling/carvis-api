package cloud.carvis.api.model.dtos

enum class ImageSize {
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
            throw RuntimeException("ImageSize [$this] can not be converted to Integer", e)
        }
}
