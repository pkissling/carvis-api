package cloud.carvis.api.service

import cloud.carvis.api.dao.repositories.CarRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service("authorization")
class AuthorizationService(private val carRepository: CarRepository) {

    @Cacheable("authorization", key = "#id + '_' + @authorization.username")
    fun canAccessCar(id: UUID): Boolean =
        isAdmin() || isCarOwner(id)

    fun isCarOwner(id: UUID): Boolean {
        val car = carRepository.findByIdOrNull(id) ?: return false
        return car.createdBy == getUsername()
    }

    fun isAdmin() =
        SecurityContextHolder.getContext().authentication.authorities
            .map { it.authority }
            .any { it == ADMIN_ROLE }

    fun getUsername(): String? =
        SecurityContextHolder.getContext().authentication.name

    companion object {
        const val ADMIN_ROLE = "ROLE_ADMIN"
    }
}
