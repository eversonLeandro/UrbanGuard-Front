package co.edu.unicauca.urbanguard.data.remote.api.dto

// Solicitud de vinculación tal como la devuelve el backend (UG-49).
data class LinkRequestDto(
    val id: String,
    val nombreSolicitante: String,
    val emailSolicitante: String,
    val conjuntoId: String,
    val conjuntoNombre: String,
    val fechaSolicitud: String,
    val codigoVinculacion: String,
    val unidad: String,
    val estado: String
)

// Cuerpo para aprobar (approve=true) o rechazar (approve=false) una solicitud.
data class ReviewLinkRequestRequestDto(
    val approve: Boolean
)