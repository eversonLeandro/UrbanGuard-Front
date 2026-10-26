package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import java.time.Instant

/**
 * Rondas de patrullaje del guardia (UG-41).
 *
 * Las ejecuciones remotas guardan una foto de puntos al iniciar, para que
 * el historial no dependa de la ruta actual.
 */
interface PatrolRoundRepository {

    /** Abre una ronda con la foto fija de puntos de la ruta. */
    suspend fun startRound(
        complexId: String,
        routeId: String,
        routeName: String,
        points: List<PatrolControlPoint>,
        scheduledAt: Instant?,
        dueAt: Instant?,
        occurrenceId: String? = null
    ): PatrolRound

    /**
     * Cierra como vencidas las abiertas que pasaron su límite. Devuelve la
     * más reciente cerrada (para mostrar su resumen) o null.
     */
    suspend fun closeOverdueRounds(complexId: String): PatrolRound?

    /** Ronda abierta del conjunto, o null si no hay ninguna. */
    suspend fun activeRound(complexId: String): PatrolRound?

    /**
     * Registra una lectura y devuelve la ronda actualizada junto al punto
     * coincidente (null si el tag no pertenece al snapshot de la ronda).
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
