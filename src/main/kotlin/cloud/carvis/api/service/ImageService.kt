package cloud.carvis.api.service

import cloud.carvis.api.model.dtos.ImageDto
import cloud.carvis.api.model.dtos.ImageHeight
import cloud.carvis.api.model.dtos.ImageHeight.ORIGINAL
import cloud.carvis.api.properties.S3Buckets
import com.amazonaws.HttpMethod
import com.amazonaws.HttpMethod.GET
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.IOUtils
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifIFD0Directory
import mu.KotlinLogging
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Rotation
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
    s3Properties: S3Buckets
) {

    private val logger = KotlinLogging.logger {}
    private val bucketName = s3Properties.images

    @Cacheable("imageUrls", sync = true)
    fun fetchImage(id: UUID, height: ImageHeight): ImageDto {
        val exists = imageExists(id, height)
        if (exists) {
            val expiresAt = now().plus(12, HOURS)
            val url = generatePresignedUrl(id, height, GET, expiresAt)
            return ImageDto(id, url, height, expiresAt)
        }

        val originalExists = imageExists(id, ORIGINAL)
        if (originalExists) {
            logger.info { "Resizing image [$id] from ORIGINAL to [$height]" }
            val stopWatch = startTimer()
            val (contentType, originalImage) = getObject(id, ORIGINAL)
            val (resizedImage, resizedLength) = resizeImage(id, contentType, originalImage, height)
            putObject(id, resizedImage, height, contentType, resizedLength)
            val took = finishTimer(stopWatch)
            logger.info { "Finished resizing image [$id] from ORIGINAL to [$height]. Took ${took}ms to resize with ${originalImage.size / 1024}KB to ${resizedLength / 1024}KB" }
            return this.fetchImage(id, height)
        }

        logger.info { "Image with id [$id] not found" }
        throw ResponseStatusException(NOT_FOUND, "image not found")
    }

    fun generateImageUploadUrl(contentType: MediaType): ImageDto {
        val id = UUID.randomUUID()
        val size = ORIGINAL
        val expiresAt = now().plus(12, HOURS)
        val url = generatePresignedUrl(id, size, HttpMethod.PUT, expiresAt, contentType)
        return ImageDto(id, url, size, expiresAt)
    }

    private fun finishTimer(stopWatch: StopWatch): Long {
        stopWatch.stop()
        return stopWatch.totalTimeMillis
    }

    private fun startTimer(): StopWatch =
        StopWatch()
            .apply { start() }

    private fun putObject(id: UUID, inputStream: InputStream, height: ImageHeight, contentType: MediaType, length: Long) = try {
        val metaData = ObjectMetadata().apply {
            this.contentType = contentType.toString()
            this.contentLength = length
        }
        s3Client.putObject(this.bucketName, "$id/$height", inputStream, metaData)
            .also { logger.debug { "Saved s3 object [$id/$height]" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to save file: $id/$height" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "unable to save file in s3")
    }

    private fun imageExists(id: UUID, height: ImageHeight): Boolean = try {
        s3Client.doesObjectExist(this.bucketName, "$id/$height")
            .also { logger.debug { "Image [$id/$height] exists: $it" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to check if image exists in s3: $id/$height" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "unable to check image in s3")
    }

    private fun generatePresignedUrl(
        id: UUID,
        height: ImageHeight,
        method: HttpMethod,
        expiresAt: Instant,
        contentType: MediaType? = null
    ): URL = try {
        s3Client.generatePresignedUrl(
            GeneratePresignedUrlRequest(this.bucketName, "$id/$height")
                .withMethod(method)
                .withContentType(contentType?.toString())
                .withExpiration(Date.from(expiresAt))
        )
    } catch (e: Exception) {
        logger.error(e) {
            "Exception caught while generating presigned URL for path " +
                    "[$id/$height], expiration [$expiresAt] and contentType [$contentType]"
        }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "communication error while generating presigned url", e)
    }

    private fun getObject(id: UUID, height: ImageHeight): Pair<MediaType, ByteArray> = try {
        val obj = s3Client.getObject(this.bucketName, "$id/$height")
        val mediaType = MediaType.valueOf(obj.objectMetadata.contentType)
        mediaType to IOUtils.toByteArray(obj.objectContent)
    } catch (e: Exception) {
        logger.error(e) { "Unable to fetch image: $id/$height" }
        throw ResponseStatusException(INTERNAL_SERVER_ERROR, "cannot fetch image from s3")
    }

    private fun resizeImage(id: UUID, contentType: MediaType, bytes: ByteArray, height: ImageHeight): Pair<InputStream, Long> =
        try {
            val rotation = calculateRotation(bytes.inputStream())
            val image = ImageIO.read(bytes.inputStream())
            val resizedImage = Scalr.resize(image, height.toInt())
                .let { img -> rotation?.let { Scalr.rotate(img, rotation) } ?: img }
            ByteArrayOutputStream()
                .also { ImageIO.write(resizedImage, contentType.subtype, it) }
                .let { ByteArrayInputStream(it.toByteArray()) to it.size().toLong() }
        } catch (e: Exception) {
            logger.error(e) { "Failed to resize image with id: $id" }
            throw ResponseStatusException(INTERNAL_SERVER_ERROR, "failed to resize image")
        }

    private fun calculateRotation(image: InputStream): Rotation? {
        val metadata: Metadata = ImageMetadataReader.readMetadata(image)
        val exifIFD0: ExifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java) ?: return null
        return when (exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
            6 -> Rotation.CW_90
            3 -> Rotation.CW_180
            8 -> Rotation.CW_270
            else -> null
        }
    }
}
