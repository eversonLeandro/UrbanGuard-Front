package co.edu.unicauca.urbanguard.domain.model

import java.time.DayOfWeek
import java.time.Instant

/**
 * Ronda de patrullaje en curso o terminada (UG-41).
 *
 * Guarda los datos de la ruta para mostrarlos sin depender del listado, y
 * cada lectura con su hora: es la constancia del paso del guardia.
 */
data class PatrolRound(
    val id: String,
    val complexId: String,
    val routeId: String,
    val routeName: String,
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    val scans: List<PatrolScan> = emptyList(),
    /**
     * Puntos de la ruta al abrir (foto fija). Sin esto no se sabría contra
     * cuántos comparar al cerrar: es lo que distingue COMPLETADA de
     * INCOMPLETA. En rondas viejas vale 0 y toda cerrada cuenta completada.
     */
    val totalPoints: Int = 0
)

/** Una lectura de tag durante la ronda. */
data class PatrolScan(
    val pointId: String?,
    val tagId: String,
    val scannedAt: Instant,
    val known: Boolean
)

/** Puntos distintos efectivamente marcados (ignora repetidos y ajenos). */
fun PatrolRound.donePointIds(): Set<String> =
    scans.filter { it.known }.mapNotNull { it.pointId }.toSet()

/** Progreso 0..1 sobre el total de puntos de la ruta. */
fun PatrolRound.progressOf(totalPoints: Int): Float {
    if (totalPoints <= 0) return 0f
    return (donePointIds().size.toFloat() / totalPoints).coerceIn(0f, 1f)
}

/** Verdadero si la ronda ya se cerró. */
fun PatrolRound.isFinished(): Boolean = finishedAt != null

/** Semáforo de la ronda para el historial. */
enum class RoundStatus {
    IN_PROGRESS,
    COMPLETED,
    INCOMPLETE
}

/**
 * Estado: en curso si está abierta; al cerrar, completada solo si marcó
 * todos los puntos que la ruta tenía al abrir.
 */
fun PatrolRound.status(): RoundStatus =
    when {
        !isFinished() -> RoundStatus.IN_PROGRESS
        totalPoints > 0 && donePointIds().size < totalPoints -> RoundStatus.INCOMPLETE
        else -> RoundStatus.COMPLETED
    }

/**
 * Busca el punto cuyo tag coincide (insensible a mayúsculas) o null si el
 * tag no pertenece a la ruta.
 */
fun matchScan(
    tagId: String,
    points: List<PatrolControlPoint>
): PatrolControlPoint? {
    val normalized = tagId.trim().uppercase()
    if (normalized.isEmpty()) return null
    return points.firstOrNull { it.nfcTagId.trim().uppercase() == normalized }
}

/** Rutas con al menos un horario el día indicado. */
fun List<PatrolRoute>.forDay(day: DayOfWeek): List<PatrolRoute> =
    filter { route -> route.isActive && route.schedules.any { it.days.contains(day) } }
