package cloud.carvis.backend.service

import cloud.carvis.backend.properties.S3Properties
import com.amazonaws.HttpMethod.GET
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.*


@Service
class ImageService(
    private val s3Client: AmazonS3,
    s3Properties: S3Properties
) {

    private val logger = KotlinLogging.logger {}
    private val bucketName = s3Properties.bucketNames["images"]

    init {
        assert(s3Properties.bucketNames["images"] != null)
    }

    fun resolveImageUrls(ids: List<UUID>): List<URL> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        return ids
            .mapNotNull { this.resolveImageId(it) }
            .toList()
    }

    fun resolveImageId(uuid: UUID): URL? {
        val smallImageExists = imageExists(uuid, "200")
        if (smallImageExists) {
            return fetchObject(uuid, "200")
        }

        val originalImageExists = imageExists(uuid, "original")
        if (originalImageExists) {
            return fetchObject(uuid, "original")
        }

        return null
    }

    private fun imageExists(uuid: UUID, fileName: String): Boolean =
        s3Client.doesObjectExist(this.bucketName, "$uuid/$fileName").also {
            logger.debug { "Image [$uuid/$fileName] exists: $it" }
        }


    private fun fetchObject(uuid: UUID, fileName: String): URL? {
        val expiration = now().plus(7, ChronoUnit.DAYS)
            .let { Date.from(it) }

        return try {
            s3Client.generatePresignedUrl(
                GeneratePresignedUrlRequest(this.bucketName, "$uuid/$fileName")
                    .withMethod(GET)
                    .withExpiration(expiration)
            )
        } catch (e: Exception) {
            logger.error("Exception caught while generating presigned URL for file [$uuid/$fileName]", e)
            null
        }
    }
}