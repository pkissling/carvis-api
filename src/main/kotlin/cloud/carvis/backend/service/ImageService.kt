package cloud.carvis.backend.service

import cloud.carvis.backend.model.dtos.ImageDto
import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.HttpMethod.GET
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
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

    fun fetchImage(id: UUID, size: String): ImageDto? {
        val exists = imageExists(id, size)
        if (!exists) {
            return null
        }

        return fetchObject(id, size)
    }

    private fun imageExists(id: UUID, size: String): Boolean =
        s3Client.doesObjectExist(this.bucketName, "$id/$size").also {
            logger.debug { "Image [$id/$size] exists: $it" }
        }


    private fun fetchObject(id: UUID, size: String): ImageDto? = try {
        val expiration = now().plus(7, ChronoUnit.DAYS)
        val url = s3Client.generatePresignedUrl(
            GeneratePresignedUrlRequest(this.bucketName, "$id/$size")
                .withMethod(GET)
                .withExpiration(Date.from(expiration))
        )
        ImageDto(id, url, size, expiration)
    } catch (e: Exception) {
        logger.error(e) { "Exception caught while generating presigned URL for file [$id/$size]" }
        null
    }
}
