package cloud.carvis.backend.model.dtos

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class CarDto(
    var id: UUID? = null,
    var brand: String? = null,
    var bodyType: String? = null,
    var ads: List<String> = emptyList(),
    var additionalEquipment: String? = null,
    var capacity: Long? = null,
    var colorAndMaterialInterior: String? = null,
    var colorExterior: String? = null,
    var colorExteriorManufacturer: String? = null,
    var condition: String? = null,
    var countryOfOrigin: String? = null,
    var createdAt: Instant? = null,
    var description: String? = null,
    var horsePower: Long? = null,
    var images: List<UUID> = emptyList(),
    var mileage: Long? = null,
    var modelDetails: String? = null,
    var modelSeries: String? = null,
    var modelYear: String? = null,
    var ownerName: String? = null,
    var ownerUsername: String? = null,
    var price: BigDecimal? = null,
    var shortDescription: String? = null,
    var transmission: String? = null,
    var type: String? = null,
    var updatedAt: Instant? = null,
    var vin: String? = null
// TODO modified by?
)
