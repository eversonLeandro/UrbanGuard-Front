package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Novedad devuelta por el backend (contrato propuesto para UG-99).
 *
 * GET `api/complexes/{complexId}/incidents` → 200 OK con la lista.
 * `photoUrl` la sirve el backend cuando la foto ya subió; en local se usa
 * la ruta del archivo.
 */
data class IncidentResponseDto(
    val id: String,
    val category: String,
    val description: String,
    val photoUrl: String? = null,
    val pointId: String? = null,
    val pointName: String? = null,
    val createdAt: String,
    val synced: Boolean = true
)
