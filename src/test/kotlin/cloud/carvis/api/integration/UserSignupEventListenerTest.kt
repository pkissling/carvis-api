package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import org.junit.jupiter.api.Test


class UserSignupEventListenerTest : AbstractApplicationTest() {

    @Test
    fun onMessage() {
        testDataGenerator.withUserSignupEvent()
    }
}
