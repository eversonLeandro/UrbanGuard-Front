package co.edu.unicauca.urbanguard.data.remote.api.dto

import com.google.gson.annotations.SerializedName

// Solicitud de vinculación tal como la devuelve el backend (UG-49).
data class LinkRequestDto(
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

// Cuerpo para aprobar (approve=true) o rechazar (approve=false) una solicitud.
data class ReviewLinkRequestRequestDto(
    val approve: Boolean
)

data class RejectLinkRequestDto(
    val reason: String? = null
)