package cloud.carvis.api.service

import cloud.carvis.api.dao.repositories.CarRepository
import cloud.carvis.api.dao.repositories.RequestRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service("authorization")
class AuthorizationService(
    private val carRepository: CarRepository,
    private val requestRepository: RequestRepository
) {

    @Cacheable("carsAuthorization", key = "#carId + '_' + @authorization.username")
    fun canAccessCar(carId: UUID): Boolean =
        isAdmin() || isCarOwner(carId)

    @Cacheable("requestsAuthorization", key = "#requestId + '_' + @authorization.username")
    fun canModifyRequest(requestId: UUID): Boolean =
        isAdmin() || isRequestOwner(requestId)

    private fun isRequestOwner(id: UUID): Boolean {
        val request = requestRepository.findByIdOrNull(id) ?: return false
        return request.createdBy == getUsername()
    }

    fun isCarOwner(id: UUID): Boolean {
        val car = carRepository.findByIdOrNull(id) ?: return false
        return car.createdBy == getUsername()
    }

    fun isAdmin() =
        SecurityContextHolder.getContext().authentication.authorities
            .map { it.authority }
            .any { it == ADMIN_ROLE }

    fun getUsername(): String =
        SecurityContextHolder.getContext().authentication.name
            ?: throw RuntimeException("Unable to get username from current context")

    companion object {
        const val ADMIN_ROLE = "ROLE_ADMIN"
    }
}
