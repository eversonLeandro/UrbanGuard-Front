package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident

/**
 * Novedades de vigilancia del guardia (UG-43).
 *
 * Offline-first: todo se guarda local al instante; con el backend
 * encendido se envía (foto incluida) y se marca sincronizado.
 */
interface IncidentRepository {

    /** Novedades del conjunto, más recientes primero. */
    suspend fun getIncidents(complexId: String): Result<List<SecurityIncident>>

    /** Guarda una novedad y trata de enviarla si el backend está activo. */
    suspend fun reportIncident(
        complexId: String,
        category: IncidentCategory,
        description: String,
        photoPath: String?,
        pointId: String?,
        pointName: String?
    ): Result<SecurityIncident>

    /** Reintenta enviar las pendientes del conjunto. */
    suspend fun retryPending(complexId: String): Result<Unit>

    /** Novedad elegida en la bandeja (detalle sin argumentos). */
    fun setSelectedIncident(incident: SecurityIncident?)

    fun selectedIncident(): SecurityIncident?
}
