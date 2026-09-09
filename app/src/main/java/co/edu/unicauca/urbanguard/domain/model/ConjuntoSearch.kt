package co.edu.unicauca.urbanguard.domain.model

data class ConjuntoSearch(
    val id: String,
    val nombre: String,
    val direccion: String,
    val ciudad: String,
    val totalMiembros: Int
)

data class LinkRequest(
    val conjuntoId: String,
    val fechaSolicitud: String,
    val estado: String = "PENDIENTE"
)
