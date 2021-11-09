package cloud.carvis.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
@Component
@ConfigurationProperties("s3-buckets")
class S3Properties {

    @NotEmpty
    lateinit var bucketNames: Map<String, String>

}
