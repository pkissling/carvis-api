package cloud.carvis.backend.model.dtos

import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Positive

data class CarDto(
    var id: UUID? = null,

    @field:NotEmpty
    var brand: String? = null,

    @field:NotEmpty
    var bodyType: String? = null,

    var ads: List<String> = emptyList(),

    var additionalEquipment: String? = null,

    @field:Positive
    var capacity: Long? = null,

    @field:NotEmpty
    var colorAndMaterialInterior: String? = null,

    @field:NotEmpty
    var colorExterior: String? = null,

    @field:NotEmpty
    var colorExteriorManufacturer: String? = null,

    @field:NotEmpty
    var condition: String? = null,

    var countryOfOrigin: String? = null,

    var createdAt: Instant? = null,

    var description: String? = null,

    @field:Positive
    var horsePower: Long? = null,

    var images: List<UUID> = emptyList(),

    @field:Min(0)
    var mileage: Long? = null,

    @field:NotEmpty
    var modelDetails: String? = null,

    @field:NotEmpty
    var modelSeries: String? = null,

    @field:NotEmpty
    var modelYear: String? = null,

    var ownerName: String? = null,

    var ownerUsername: String? = null,

    var price: BigDecimal? = null,

    @field:NotEmpty
    var shortDescription: String? = null,

    @field:NotEmpty
    var transmission: String? = null,

    @field:NotEmpty
    var type: String? = null,

    var updatedAt: Instant? = null,

    var vin: String? = null
// TODO modified by?
)
