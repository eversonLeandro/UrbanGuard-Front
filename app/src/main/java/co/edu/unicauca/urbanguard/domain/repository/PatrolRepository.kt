package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint

/**
 * Puntos de control NFC de la ruta de patrullaje (UG-40).
 *
 * Hoy resuelve contra el almacén local del equipo; cuando el backend esté
 * listo la firma no cambia: solo se enciende el flag y el repositorio
 * sincroniza contra `PatrolApi`.
 */
interface PatrolRepository {

    /** Puntos del conjunto ordenados para la ronda. */
    suspend fun getPoints(complexId: String): Result<List<PatrolControlPoint>>

    /**
     * Crea (id en blanco) o actualiza un punto. El `order` se asigna solo:
     * al crear va al final de la ruta.
     */
    suspend fun savePoint(point: PatrolControlPoint): Result<PatrolControlPoint>

    /** Elimina un punto y compacta el orden de la ruta. */
    suspend fun deletePoint(complexId: String, pointId: String): Result<Unit>

    /** Mueve un punto a una nueva posición y reordena la ruta. */
    suspend fun movePoint(complexId: String, pointId: String, toOrder: Int): Result<Unit>

    /** Activa o desactiva un punto sin sacarlo de la ruta. */
    suspend fun setPointActive(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit>
}
