package cloud.carvis.api.service

import cloud.carvis.api.dao.repositories.RequestRepository
import cloud.carvis.api.mapper.RequestMapper
import cloud.carvis.api.model.dtos.RequestDto
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class RequestService(
    private val authorizationService: AuthorizationService,
    private val requestRepository: RequestRepository,
    private val requestMapper: RequestMapper
) {

    fun fetchRequests(): List<RequestDto> {
        val isAdmin = authorizationService.isAdmin()
        val username = authorizationService.getUsername() ?: throw ResponseStatusException(
            INTERNAL_SERVER_ERROR,
            "Unable to get username from current context"
        )

        val requests = when (isAdmin) {
            true -> requestRepository.findAll()
            false -> requestRepository.findAllByCreatedBy(username)
        }

        return requests
            .map { requestMapper.toDto(it) }
            .toList()
    }

}
