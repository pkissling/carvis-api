package cloud.carvis.api.common.auth.service

import cloud.carvis.api.cars.dao.CarRepository
import cloud.carvis.api.requests.dao.RequestRepository
import cloud.carvis.api.users.model.UserRole.ADMIN
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service("authorization")
class AuthorizationService(
    private val carRepository: CarRepository,
    private val requestRepository: RequestRepository
) {

    @Cacheable("cars-authorization", key = "#carId + '_' + @authorization.userId")
    fun canModifyCar(carId: UUID): Boolean =
        isAdmin() || isCarOwner(carId)

    @Cacheable("requests-authorization", key = "#requestId + '_' + @authorization.userId")
    fun canModifyRequest(requestId: UUID): Boolean =
        isAdmin() || isRequestOwner(requestId)

    @Cacheable("users-authorization", key = "#userId + '_' + @authorization.userId")
    fun canAccessAndModifyUser(userId: String): Boolean =
        isAdmin() || isUser(userId)

    private fun isRequestOwner(id: UUID): Boolean {
        val request = requestRepository.findByIdOrNull(id) ?: return false
        return request.createdBy == getUserId()
    }

    fun isCarOwner(id: UUID): Boolean {
        val car = carRepository.findByIdOrNull(id) ?: return false
        return car.createdBy == getUserId()
    }

    fun isAdmin() =
        SecurityContextHolder.getContext().authentication.authorities
            .map { it.authority }
            .any { ADMIN.isRole(it) }

    fun getUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is AnonymousAuthenticationToken || authentication.name.isEmpty()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot obtain userId from anonymous user")
        }
        return authentication.name
    }

    fun isUser(getUserId: String): Boolean =
        getUserId() == getUserId
}
