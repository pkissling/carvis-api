package cloud.carvis.backend.auth

import cloud.carvis.backend.dao.repositories.CarRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service("authorization")
class AuthorizationService(private val carRepository: CarRepository) {

    // TODO cache
    fun canAccessCar(id: UUID): Boolean =
        isAdmin() || isCarOwner(id)


    private fun isCarOwner(id: UUID): Boolean {
        val car = carRepository.findByIdOrNull(id) ?: return false
        return car.ownerUsername == username()
    }

    private fun username(): String? =
        SecurityContextHolder.getContext().authentication?.name


    fun isAdmin() =
        SecurityContextHolder.getContext().authentication.authorities
            .map { it.authority }
            .any { it == ADMIN_ROLE }

    companion object {
        const val ADMIN_ROLE = "ROLE_ADMIN"
    }

}
