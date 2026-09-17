package co.edu.unicauca.urbanguard.data.remote.api.dto

data class ConjuntoSearchResponseDto(
    val id: String,
    val nombre: String,
    val direccion: String,
    val ciudad: String,
    val totalMiembros: Int
)

data class LinkRequestResponseDto(
    val conjuntoId: String,
    val fechaSolicitud: String,
    val estado: String,
    // TODO: cuando el backend incluya la unidad en la respuesta, quitar el default.
    // Por ahora el frontend conserva el apartamento ingresado por el residente.
    val unidad: String = ""
)

// Cuerpo para crear la solicitud de vinculación: incluye el número de
// apartamento/unidad del residente que solicita.
data class CreateLinkRequestRequestDto(
    val unidad: String
)
