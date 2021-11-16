package cloud.carvis.api.service

import cloud.carvis.api.dao.repositories.RequestRepository
import cloud.carvis.api.mapper.RequestMapper
import cloud.carvis.api.model.dtos.RequestDto
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class RequestService(
    private val authorizationService: AuthorizationService,
    private val requestRepository: RequestRepository,
    private val requestMapper: RequestMapper
) {

    private val logger = KotlinLogging.logger {}

    fun fetchAllRequests(): List<RequestDto> {
        return requestRepository.findAll()
            .map { requestMapper.toDto(it) }
            .toList()
    }

    fun fetchRequest(id: UUID): RequestDto {
        val request = requestRepository.findByIdOrNull(id)
        if (request == null) {
            logger.info { "Request with id [$id] not found" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "request not found")
        }

        return request
            .let { requestMapper.toDto(it) }
    }
}
