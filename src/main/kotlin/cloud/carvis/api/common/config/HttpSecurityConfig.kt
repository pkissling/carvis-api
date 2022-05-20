package cloud.carvis.api.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler

import org.springframework.security.web.firewall.RequestRejectedHandler


@Configuration
class HttpSecurityConfig {

    @Bean
    fun requestRejectedHandler(): RequestRejectedHandler {
        return HttpStatusRequestRejectedHandler()
    }

}
