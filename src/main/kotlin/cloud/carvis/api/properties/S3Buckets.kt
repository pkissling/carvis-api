package cloud.carvis.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull
import kotlin.reflect.full.declaredMemberProperties

@Validated
@Component
@ConfigurationProperties("s3.buckets")
class S3Buckets : Iterable<String> {

    @NotNull
    lateinit var images: String

    override fun iterator(): Iterator<String> =
        S3Buckets::class.declaredMemberProperties
            .map { it.getValue(this, it) }
            .map { it.toString() }
            .iterator()
}
