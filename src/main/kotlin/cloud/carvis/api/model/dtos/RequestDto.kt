package cloud.carvis.api.model.dtos

import java.time.Instant
import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Positive

data class RequestDto(
    // technical field
    var id: UUID? = null,

    // Mehr-Ausstattung
    var additionalEquipment: String? = null,

    // Karosserie
    var bodyType: String? = null,

    // Fahrzeughersteller
    @field:NotEmpty
    var brand: String? = null,

    // Budget in Euro
    var budget: String? = null,

    // Hubraum in ccm
    @field:Positive
    var capacity: Long? = null,

    // Auslieferungsland
    var countryOfOrigin: String? = null,

    // Außenfarbe
    var colorExterior: String? = null,

    // Hersteller Farbbezeichnung
    var colorExteriorManufacturer: String? = null,

    // Innenmaterial und Farbbez.
    var colorAndMaterialInterior: String? = null,

    // Zustand
    var condition: String? = null,

    // technical field
    var createdAt: Instant? = null,

    // technical field
    var createdBy: String? = null,

    // Sonstiges/Beschreibung
    var description: String? = null,

    // technical field
    var hasHiddenFields: Boolean? = null,

    // Besonderheit/ Highlights
    var highlights: String? = null,

    // Leistung in PS
    @field:Positive
    var horsePower: Long? = null,

    // Laufleistung (km/mls)
    @field:Min(0)
    var mileage: Long? = null,

    // Must-Have
    var mustHaves: String? = null,

    // No-Go
    var noGos: String? = null,

    // technical field
    var ownerName: String? = null,

    // Modellreihe
    var modelSeries: String? = null,

    // Modelljahr
    var modelYear: String? = null,

    // Modellspezifikation
    var modelSpecification: String? = null,

    // Getriebe
    var transmission: String? = null,

    // Typ
    @field:NotEmpty
    var type: String? = null,

    // technical field
    var updatedAt: Instant? = null,

    // technical field
    var updatedBy: String? = null,

    // Ziel/Vision
    var vision: String? = null
)
