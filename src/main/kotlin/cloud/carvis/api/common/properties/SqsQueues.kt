package cloud.carvis.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull
import kotlin.reflect.full.declaredMemberProperties

@Validated
@Component
@ConfigurationProperties("sqs.queues")
class SqsQueues : Iterable<String> {

    @NotNull
    lateinit var userSignup: String

    @NotNull
    lateinit var carvisCommand: String

    @NotNull
    lateinit var carvisEvent: String

    override fun iterator(): Iterator<String> =
        SqsQueues::class.declaredMemberProperties
            .map { it.getValue(this, it) }
            .map { it.toString() }
            .iterator()

}
