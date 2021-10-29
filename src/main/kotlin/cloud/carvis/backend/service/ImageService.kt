package cloud.carvis.backend.service

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URL
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.*


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
        if (!exists) {
            logger.info { "Image with id [$id] not found" }
            throw ResponseStatusException(NOT_FOUND, "image not found")
        }

        val expiration = now().plus(7, ChronoUnit.DAYS)
        val url = generatePresignedUrl(id, size, HttpMethod.GET, expiration)
        return ImageDto(id, url, size, expiration)
    }

    fun createPresignedUrl(contentType: MediaType): ImageDto {
        val id = UUID.randomUUID()
        val size = "original"
        val expiration = now().plus(1, ChronoUnit.DAYS)
        val url = generatePresignedUrl(id, size, HttpMethod.PUT, expiration, contentType)
        return ImageDto(id, url, size, expiration)
    }

    private fun imageExists(id: UUID, size: String): Boolean =
        s3Client.doesObjectExist(this.bucketName, "$id/$size").also {
            logger.debug { "Image [$id/$size] exists: $it" }
        }


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
}
