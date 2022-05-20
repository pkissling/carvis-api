package cloud.carvis.api.images.service

import cloud.carvis.api.common.properties.S3Buckets
import cloud.carvis.api.images.model.ImageDto
import cloud.carvis.api.images.model.ImageHeight
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CopyObjectRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.IOUtils
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifIFD0Directory
import mu.KotlinLogging
import org.imgscalr.Scalr
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    fun fetchImage(imageId: UUID, height: ImageHeight): ImageDto {
        val exists = imageExists("$imageId/$height")
        if (exists) {
            val key = "$imageId/$height"
            val expiresAt = Instant.now().plus(12, ChronoUnit.HOURS)
            val url = generatePresignedUrl(key, HttpMethod.GET, expiresAt)
            return ImageDto(imageId, url, height, expiresAt)
        }

        val originalExists = imageExists("$imageId/${ImageHeight.ORIGINAL}")
        if (originalExists) {
            logger.info { "Resizing image [$imageId] from ORIGINAL to [$height]" }
            val stopWatch = startTimer()
            val (contentType, originalImage) = getObject("$imageId/${ImageHeight.ORIGINAL}")
            val (resizedImage, resizedLength) = resizeImage(imageId, contentType, originalImage, height)
            putObject("$imageId/$height", resizedImage, contentType, resizedLength)
            val took = finishTimer(stopWatch)
            logger.info { "Finished resizing image [$imageId] from ORIGINAL to [$height]. Took ${took}ms to resize with ${originalImage.size / 1024}KB to ${resizedLength / 1024}KB" }
            return this.fetchImage(imageId, height)
        }

        logger.info { "Image with id [$imageId] not found" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "image not found")
    }

    fun generateImageUploadUrl(contentType: MediaType): ImageDto {
        val id = UUID.randomUUID()
        val size = ImageHeight.ORIGINAL
        val expiresAt = Instant.now().plus(12, ChronoUnit.HOURS)
        val key = "$id/$size"
        val url = generatePresignedUrl(key, HttpMethod.PUT, expiresAt, contentType)
        return ImageDto(id, url, size, expiresAt)
    }

    fun deleteImage(imageId: UUID) {
        logger.info { "Deleting imageId: $imageId" }
        this.delete("$imageId")
    }

    private fun finishTimer(stopWatch: StopWatch): Long {
        stopWatch.stop()
        return stopWatch.totalTimeMillis
    }

    private fun startTimer(): StopWatch =
        StopWatch()
            .apply { start() }

    private fun putObject(key: String, inputStream: InputStream, contentType: MediaType, length: Long) = try {
        val metaData = ObjectMetadata().apply {
            this.contentType = contentType.toString()
            this.contentLength = length
        }
        s3Client.putObject(this.bucketName, key, inputStream, metaData)
            .also { logger.debug { "Saved s3 object [$key]" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to save file: $key" }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to save file in s3")
    }

    private fun imageExists(key: String): Boolean = try {
        s3Client.doesObjectExist(this.bucketName, key)
            .also { logger.debug { "Image [$key] exists: $it" } }
    } catch (e: Exception) {
        logger.error(e) { "Unable to check if image exists in s3: $key" }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to check image in s3")
    }

    private fun generatePresignedUrl(key: String, method: HttpMethod, expiresAt: Instant, contentType: MediaType? = null): URL = try {
        s3Client.generatePresignedUrl(
            GeneratePresignedUrlRequest(this.bucketName, key)
                .withMethod(method)
                .withContentType(contentType?.toString())
                .withExpiration(Date.from(expiresAt))
        )
    } catch (e: Exception) {
        logger.error(e) {
            "Exception caught while generating presigned URL for key " +
                    "[$key], expiration [$expiresAt] and contentType [$contentType]"
        }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "communication error while generating presigned url", e)
    }

    private fun getObject(key: String): Pair<MediaType, ByteArray> {
        try {
            val obj = s3Client.getObject(this.bucketName, key)
            val mediaType = MediaType.valueOf(obj.objectMetadata.contentType)
            return mediaType to IOUtils.toByteArray(obj.objectContent)
        } catch (e: Exception) {
            logger.error(e) { "Unable to fetch image: $key" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot fetch image from s3")
        }
    }

    private fun resizeImage(imageId: UUID, contentType: MediaType, bytes: ByteArray, height: ImageHeight): Pair<InputStream, Long> =
        try {
            val rotation = calculateRotation(bytes.inputStream())
            if (rotation == null) {
                logger.warn { "Unable to determine extract orientation for imageId: $imageId" }
            }
            val image = ImageIO.read(bytes.inputStream())
            val resizedImage = Scalr.resize(image, height.toInt())
                .let { img -> rotation?.let { Scalr.rotate(img, rotation) } ?: img }
            ByteArrayOutputStream()
                .also { ImageIO.write(resizedImage, contentType.subtype, it) }
                .let { ByteArrayInputStream(it.toByteArray()) to it.size().toLong() }
        } catch (e: Exception) {
            logger.error(e) { "Failed to resize image with id: $imageId" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to resize image")
        }

    private fun calculateRotation(image: InputStream): Scalr.Rotation? {
        val metadata: Metadata = ImageMetadataReader.readMetadata(image)
        val exifIFD0: ExifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java) ?: return null
        val hasOrientation = exifIFD0.containsTag(ExifIFD0Directory.TAG_ORIENTATION)
        if (!hasOrientation) {
            return null
        }
        return when (exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
            6 -> Scalr.Rotation.CW_90
            3 -> Scalr.Rotation.CW_180
            8 -> Scalr.Rotation.CW_270
            else -> null
        }
    }


    private fun delete(key: String) {
        logger.debug { "Deleting image(s) with key: $key" }
        val childs = s3Client.listObjects(bucketName, key)
            .objectSummaries
            .map { it.key }
        if (childs.isEmpty()) {
            throw RuntimeException("Should have deleted key [$key], but it did not exist in S3")
        }
        childs.forEach { childKey ->
            s3Client.copyObject(
                CopyObjectRequest()
                    .withSourceBucketName(bucketName)
                    .withDestinationBucketName(bucketName)
                    .withSourceKey(childKey)
                    .withDestinationKey("deleted/$childKey")
            )
            s3Client.deleteObject(bucketName, childKey)
        }
        logger.debug { "Deleted image(s) with key: $key" }
    }

    fun assignCarIdToImage(carId: UUID, imageId: String) {
        logger.debug { "Assigning carId [$carId] to image: $imageId" }
        val key = "$imageId/${ImageHeight.ORIGINAL}"
        val currentMetadata = s3Client.getObjectMetadata(bucketName, key)
        s3Client.copyObject(
            CopyObjectRequest(bucketName, key, bucketName, key)
                .withNewObjectMetadata(currentMetadata.apply {
                    addUserMetadata("carId", "$carId")
                })
        )
        logger.debug { "Assigned carId [$carId] to image: $imageId" }
    }
}
