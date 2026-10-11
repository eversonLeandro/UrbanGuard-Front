package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound

/**
 * Rondas de patrullaje del guardia (UG-41).
 *
 * Hoy resuelve contra el almacén local del equipo; mañana sincroniza con el
 * backend sin cambiar la firma.
 */
interface PatrolRoundRepository {

    /** Abre una ronda para la ruta indicada. */
    suspend fun startRound(
        complexId: String,
        routeId: String,
        routeName: String,
        totalPoints: Int
    ): PatrolRound

    /** Ronda abierta del conjunto, o null si no hay ninguna. */
    suspend fun activeRound(complexId: String): PatrolRound?

    /**
     * Registra una lectura y devuelve la ronda actualizada junto al punto
     * coincidente (null si el tag no es de ninguna ruta del conjunto).
     */
    suspend fun registerScan(
        complexId: String,
        tagId: String
    ): Result<Pair<PatrolRound, PatrolControlPoint?>>

    /** Cierra la ronda abierta y la devuelve para el resumen. */
    suspend fun finishRound(complexId: String): Result<PatrolRound>

    /** Rondas del conjunto, más recientes primero. */
    suspend fun getRounds(complexId: String): Result<List<PatrolRound>>

    /** Última ronda cerrada en esta sesión (para el resumen sin argumentos). */
    fun lastFinishedRound(): PatrolRound?
}
