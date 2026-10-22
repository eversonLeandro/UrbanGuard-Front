package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint

/**
 * Puntos de control NFC del conjunto (UG-40).
 *
 * El backend es la fuente de verdad: la caché local solo sirve para lecturas
 * sin conexión. El orden de la ruta se guarda en [PatrolRoute.pointIds].
 */
interface PatrolRepository {

    /** Puntos del conjunto ordenados para mostrar (activos primero). */
    suspend fun getPoints(complexId: String): Result<List<PatrolControlPoint>>

    /** Crea (id en blanco) o actualiza un punto, incluido su tag NFC. */
    suspend fun savePoint(point: PatrolControlPoint): Result<PatrolControlPoint>

    /** Activa o desactiva un punto sin sacarlo de la ruta. */
    suspend fun setPointActive(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit>
}
