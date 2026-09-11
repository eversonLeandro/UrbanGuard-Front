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
    val estado: String
)
