package cloud.carvis.api.service

import cloud.carvis.api.model.dtos.ImageDto
import cloud.carvis.api.model.dtos.ImageSize
import cloud.carvis.api.model.dtos.ImageSize.ORIGINAL
import cloud.carvis.api.properties.S3Properties
import com.amazonaws.HttpMethod
import com.amazonaws.HttpMethod.GET
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import mu.KotlinLogging
import org.imgscalr.Scalr
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit.HOURS
import java.util.*
import javax.imageio.ImageIO


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

    @Cacheable("imageUrls", sync = true)
    fun fetchImage(id: UUID, size: ImageSize): ImageDto {
        val exists = imageExists(id, size)
        if (exists) {
            val expiresAt = now().plus(12, HOURS)
            val url = generatePresignedUrl(id, size, GET, expiresAt)
            return ImageDto(id, url, size, expiresAt)
        }

        val originalExists = imageExists(id, ORIGINAL)
        if (originalExists) {
            logger.info { "Resizing image [$id] from ORIGINAL to [$size]" }
            val stopWatch = startTimer()
            val (contentType, image, originalLength) = getObject(id, ORIGINAL)
            val (resizedImage, resizedLength) = resizeImage(id, contentType, image, size)
            putObject(id, resizedImage, size, contentType, resizedLength)
            val took = finishTimer(stopWatch)
            logger.info { "Finished resizing image [$id] from ORIGINAL to [$size]. Took ${took}ms to resize with ${originalLength / 1024}KB to ${resizedLength / 1024}KB" }
            return this.fetchImage(id, size)
        }

        logger.info { "Image with id [$id] not found" }
        throw ResponseStatusException(NOT_FOUND, "image not found")
    }

    private fun finishTimer(stopWatch: StopWatch): Long {
        stopWatch.stop()
        return stopWatch.totalTimeMillis
    }

    private fun startTimer(): StopWatch =
        StopWatch()
            .apply { start() }

    fun generateImageUploadUrl(contentType: MediaType): ImageDto {
        val id = UUID.randomUUID()
        val size = ORIGINAL
        val expiresAt = now().plus(12, HOURS)
        val url = generatePresignedUrl(id, size, HttpMethod.PUT, expiresAt, contentType)
        return ImageDto(id, url, size, expiresAt)
    }

    private fun putObject(id: UUID, inputStream: InputStream, size: ImageSize, contentType: MediaType, length: Long) = try {
        val metaData = ObjectMetadata().apply {
            this.contentType = contentType.toString()
            this.contentLength = length
        }
        s3Client.putObject(this.bucketName, "$id/$size", inputStream, metaData)
            .also { logger.debug { "Saved s3 object [$id/$size]" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to save file: $id/$size" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "unable to save file in s3")
    }

    private fun imageExists(id: UUID, size: ImageSize): Boolean = try {
        s3Client.doesObjectExist(this.bucketName, "$id/$size")
            .also { logger.debug { "Image [$id/$size] exists: $it" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to check if image exists in s3: $id/$size" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "unable to check image in s3")
    }

    private fun generatePresignedUrl(
        id: UUID,
        size: ImageSize,
        method: HttpMethod,
        expiresAt: Instant,
        contentType: MediaType? = null
    ): URL = try {
        s3Client.generatePresignedUrl(
            GeneratePresignedUrlRequest(this.bucketName, "$id/$size")
                .withMethod(method)
                .withContentType(contentType?.toString())
                .withExpiration(Date.from(expiresAt))
        )
    } catch (e: Exception) {
        logger.error(e) {
            "Exception caught while generating presigned URL for path " +
                    "[$id/$size], expiration [$expiresAt] and contentType [$contentType]"
        }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "communication error while generating presigned url", e)
    }

    private fun getObject(id: UUID, size: ImageSize): Triple<MediaType, InputStream, Long> = try {
        val obj = s3Client.getObject(this.bucketName, "$id/$size")
        val mediaType = MediaType.valueOf(obj.objectMetadata.contentType)
        Triple(mediaType, obj.objectContent, obj.objectMetadata.contentLength)
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch image: $id/$size" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "cannot fetch image from s3")
    }

    private fun resizeImage(id: UUID, contentType: MediaType, inputStream: InputStream, size: ImageSize): Pair<InputStream, Long> = try {
        val image = ImageIO.read(inputStream)
        val resizedImage = Scalr.resize(image, size.toInt())
        ByteArrayOutputStream()
            .also { ImageIO.write(resizedImage, contentType.subtype, it) }
            .let { ByteArrayInputStream(it.toByteArray()) to it.size().toLong() }
    } catch (e: Exception) {
        logger.error(e) { "Failed to resize image with id: $id" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "failed to resize image")
    }
}
