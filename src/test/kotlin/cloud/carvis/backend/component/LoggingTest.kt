package cloud.carvis.backend.component

import cloud.carvis.backend.service.LoggingService
import cloud.carvis.backend.util.AbstractApplicationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class LoggingTest : AbstractApplicationTest() {

    @SpyBean
    lateinit var loggingServiceSpyBean: LoggingService

    @Test
    @WithMockUser
    fun `Logging - Add generated traceId from Sentry to MDC context`() {
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
    fun `Logging - Reuse sentry-trace from HTTP request header`() {
        // given
        val sentryTraceHeader = "e06602096d1f4e679d9770cd1aa1923b-a7e5ea5d5423bc71-1"
        val sentryTraceParsed = "e06602096d1f4e679d9770cd1aa1923b"

        // when
        this.mockMvc.perform(
            get("/cars")
                .header("sentry-trace", sentryTraceHeader)
        )
            .andExpect(status().isOk)

        // then
        verify(loggingServiceSpyBean).addTraceId(sentryTraceParsed)
        verifyNoMoreInteractions(loggingServiceSpyBean)
    }
}
