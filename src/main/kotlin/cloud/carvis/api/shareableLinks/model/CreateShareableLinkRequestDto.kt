package cloud.carvis.api.shareableLinks.model

import javax.validation.constraints.NotBlank

data class CreateShareableLinkRequestDto(
    @field:NotBlank
    val recipientName: String
)
