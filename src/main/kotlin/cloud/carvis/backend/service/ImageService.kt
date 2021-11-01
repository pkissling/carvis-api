package cloud.carvis.backend.service

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import mu.KotlinLogging
import org.imgscalr.Scalr
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.*
import javax.imageio.ImageIO


@Service
// TODO retry?
class ImageService(
    private val s3Client: AmazonS3,
    s3Properties: S3Properties
) {

    private val logger = KotlinLogging.logger {}
    private val bucketName = s3Properties.bucketNames["images"]

    init {
        assert(s3Properties.bucketNames["images"] != null)
    }

    fun fetchImage(id: UUID, size: String): ImageDto {
        val exists = imageExists(id, size)
        if (exists) {
            val expiration = now().plus(7, ChronoUnit.DAYS)
            val url = generatePresignedUrl(id, size, HttpMethod.GET, expiration)
            return ImageDto(id, url, size, expiration)
        }

        val originalExists = imageExists(id, "original")
        if (originalExists) {
            logger.info { "Resizing image [$id] from original to $size" }
            val image = getObject(id, "original")
            val resizedImage = resizeImage(id, image.first, image.second, size)
            putObject(id, resizedImage, size)
            logger.info { "Finished resizing image [$id] from original to $size" }
            return this.fetchImage(id, size)
        }

        logger.info { "Image with id [$id] not found" }
        throw ResponseStatusException(NOT_FOUND, "image not found")
    }

    fun generateImageUploadUrl(contentType: MediaType): ImageDto {
        val id = UUID.randomUUID()
        val size = "original"
        val expiration = now().plus(1, ChronoUnit.DAYS)
        val url = generatePresignedUrl(id, size, HttpMethod.PUT, expiration, contentType)
        return ImageDto(id, url, size, expiration)
    }

    private fun putObject(id: UUID, file: File, size: String) = try {
        s3Client.putObject(this.bucketName, "$id/$size", file)
            .also { logger.debug { "Saved s3 object [$id/$size]" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to save file: $id/$size" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "unable to save file in s3")
    }

    private fun imageExists(id: UUID, size: String): Boolean =
        s3Client.doesObjectExist(this.bucketName, "$id/$size")
            .also { logger.debug { "Image [$id/$size] exists: $it" } }

    private fun generatePresignedUrl(
        id: UUID,
        size: String,
        method: HttpMethod,
        expiration: Instant,
        contentType: MediaType? = null
    ): URL = try {
        s3Client.generatePresignedUrl(
            GeneratePresignedUrlRequest(this.bucketName, "$id/$size")
                .withMethod(method)
                .withContentType(contentType?.toString())
                .withExpiration(Date.from(expiration))
        )
    } catch (e: Exception) {
        logger.error(e) {
            "Exception caught while generating presigned URL for path " +
                    "[$id/$size], expiration [$expiration] and contentType [$contentType]"
        }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "communication error", e)
    }

    private fun getObject(id: UUID, size: String): Pair<MediaType, InputStream> = try {
        val obj = s3Client.getObject(this.bucketName, "$id/$size")
        val mediaType = MediaType.valueOf(obj.objectMetadata.contentType)
        mediaType to obj.objectContent
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch image: $id/$size" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "cannot fetch image from s3")
    }

    private fun resizeImage(id: UUID, contentType: MediaType, inputStream: InputStream, size: String): File = try {
        val image = ImageIO.read(inputStream)
        val resizedImage = Scalr.resize(image, size.toInt())
        File.createTempFile(id.toString(), null)
            .also { ImageIO.write(resizedImage, contentType.subtype, it) }
    } catch (e: Exception) {
        logger.error(e) { "Failed to resize image with id: $id" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "failed to resize image")
    }

}
