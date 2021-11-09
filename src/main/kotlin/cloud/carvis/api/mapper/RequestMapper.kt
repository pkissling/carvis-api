package cloud.carvis.api.mapper

import cloud.carvis.api.model.dtos.RequestDto
import cloud.carvis.api.model.entities.RequestEntity
import org.springframework.stereotype.Service

@Service
class RequestMapper() : Mapper<RequestDto, RequestEntity> {

    override fun toDto(entity: RequestEntity): RequestDto =
        RequestDto()

    override fun toEntity(dto: RequestDto): RequestEntity =
        RequestEntity()
}
