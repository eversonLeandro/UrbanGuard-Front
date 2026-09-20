package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * DTO de la respuesta de GET /api/complexes (Community Service).
 *
 * Campos nullable reflejan la respuesta real del backend:
 * address, city, numberOfUnits y unit pueden no estar presentes
 * cuando el conjunto aún no tiene esa información configurada.
 * El modelo de dominio [co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation]
 * no se expande en este cambio — solo se consumen id, name y role.
 */
data class ComplexSummaryDto(
    val id: String,
    val name: String,
    val address: String?,
    val city: String?,
    val numberOfUnits: Int?,
    val role: String,
    val unit: String?
)
