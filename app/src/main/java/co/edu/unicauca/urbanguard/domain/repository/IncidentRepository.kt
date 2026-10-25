package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident

/**
 * Novedades de vigilancia del guardia (UG-43).
 *
 * El backend es la fuente única de verdad para creación y consulta.
 */
interface IncidentRepository {

    /** Novedades del conjunto, más recientes primero. */
    suspend fun getIncidents(complexId: String, isAdmin: Boolean): Result<List<SecurityIncident>>

    suspend fun getIncident(complexId: String, reportId: String): Result<SecurityIncident>

    /** Envía una novedad con foto obligatoria y devuelve la respuesta del servidor. */
    suspend fun reportIncident(
        complexId: String,
        category: IncidentCategory,
        description: String,
        photoPath: String?,
        pointId: String?,
        executionId: String?,
        idempotencyKey: String
    ): Result<SecurityIncident>
}
