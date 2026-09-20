package co.edu.unicauca.urbanguard.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class ConjuntoSearchResponseDto(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val numberOfUnits: Int,
    val units: List<UnitLookupDto> = emptyList()
)

data class UnitLookupDto(
    val id: String,
    val identifier: String
)
data class LinkRequestResponseDto(
    val id: String,
    val userId: String,
    @SerializedName("complexId")
    val conjuntoId: String,
    val unitId: String,
    val unit: String,
    val complexName: String? = null,
    val applicantName: String? = null,
    val applicantEmail: String? = null,
    val createdAt: String? = null,
    val status: String,
    val roleSyncPending: Boolean
)

// Cuerpo para crear la solicitud de vinculación: incluye el número de
// apartamento/unidad del residente que solicita.
data class CreateLinkRequestRequestDto(
    val complexId: String,
    val unitId: String
)
