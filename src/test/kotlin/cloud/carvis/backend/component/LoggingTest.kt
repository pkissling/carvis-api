package cloud.carvis.backend.component

import cloud.carvis.backend.service.LoggingService
import cloud.carvis.backend.util.AbstractApplicationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


class LoggingTest : AbstractApplicationTest() {

    @SpyBean
    lateinit var loggingServiceSpyBean: LoggingService

    @Test
    @WithMockUser
    fun `Logging - Create new traceId if not exist`() {
        // when
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)

        // then
        argumentCaptor<String>().apply {
            verify(loggingServiceSpyBean).addTraceId(capture())
            assertThat(firstValue).isNotNull
        }
    }

    @Test
    @WithMockUser
    fun `Logging - Reuse sentry-trace`() {
        // given
        val sentryTrace = UUID.randomUUID().toString()

        // when
        this.mockMvc.perform(
            get("/cars")
                .header("sentry-trace", sentryTrace)
        )
            .andExpect(status().isOk)

        // then
        argumentCaptor<String>().apply {
            verify(loggingServiceSpyBean).addTraceId(capture())
            assertThat(firstValue).isEqualTo(sentryTrace)
        }
    }
}
