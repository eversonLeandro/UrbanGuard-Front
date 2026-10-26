package co.edu.unicauca.urbanguard.domain.model

import java.time.DayOfWeek
import java.time.Instant

/**
 * Ronda de patrullaje en curso o terminada (UG-41).
 *
 * Guarda los datos de la ruta y una foto fija de sus puntos al abrir, para
 * que el resumen siga siendo correcto aunque luego se edite la ruta.
 */
data class PatrolRound(
    val id: String,
    val complexId: String,
    val routeId: String,
    val routeName: String,
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    val scans: List<PatrolScan> = emptyList(),
    /** Puntos de la ruta al abrir (foto fija). */
    val points: List<PatrolControlPoint> = emptyList(),
    /** Cita que cubre (null = ronda libre, sin vencimiento). */
    val scheduledAt: Instant? = null,
    /** Límite: fecha-hora de la siguiente ronda programada (null = libre). */
    val dueAt: Instant? = null,
    /** Por qué quedó incompleta (null si completada o abierta). */
    val incompleteReason: IncompleteReason? = null
) {
    /** Total derivado de la foto fija, nunca un contador aparte. */
    val totalPoints: Int get() = points.size
}

/** Motivo de la incompleta: cerrada a medias o vencida por tiempo. */
enum class IncompleteReason {
    POINTS_MISSING,
    TIME_OUT
}

/** Una lectura de tag durante la ronda. */
data class PatrolScan(
    val pointId: String?,
    val tagId: String,
    val scannedAt: Instant,
    val known: Boolean
)

/** Puntos de la foto fija efectivamente marcados (ignora repetidos y ajenos). */
fun PatrolRound.donePointIds(): Set<String> {
    val snapshotIds = points.mapTo(mutableSetOf()) { it.id }
    return scans.filter { it.known }.mapNotNull { it.pointId }
        .filter { it in snapshotIds }
        .toSet()
}

/** Progreso 0..1 sobre el total de puntos de la ruta. */
fun PatrolRound.progressOf(totalPoints: Int): Float {
    if (totalPoints <= 0) return 0f
    return (donePointIds().size.toFloat() / totalPoints).coerceIn(0f, 1f)
}

/** Verdadero si la ronda ya se cerró. */
fun PatrolRound.isFinished(): Boolean = finishedAt != null

/**
 * Verdadero si sigue abierta pero ya pasó su límite.
 * Las rondas sin [dueAt] (rondas libres) se consideran vencidas
 * después de [maxFreeHours] horas para evitar que bloqueen nuevas rondas.
 */
fun PatrolRound.isOverdue(
    now: Instant = Instant.now(),
    maxFreeHours: Long = 8L
): Boolean {
    if (isFinished()) return false
    val due = dueAt
    return if (due != null) {
        now.isAfter(due)
    } else {
        // Ronda libre: vence automáticamente pasadas maxFreeHours desde el inicio
        now.isAfter(startedAt.plusSeconds(maxFreeHours * 3600))
    }
}

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
        incompleteReason != null -> RoundStatus.INCOMPLETE
        totalPoints > 0 && donePointIds().size < totalPoints -> RoundStatus.INCOMPLETE
        else -> RoundStatus.COMPLETED
    }

/**
 * Busca el punto cuyo tag coincide (insensible a mayúsculas y separadores) o
 * null si el tag no pertenece a la ruta.
 */
fun matchScan(
    tagId: String,
    points: List<PatrolControlPoint>
): PatrolControlPoint? {
    val normalized = normalizeNfcUid(tagId)
    if (normalized.isEmpty()) return null
    return points.firstOrNull { normalizeNfcUid(it.nfcTagId) == normalized }
}

/** Rutas con al menos un horario el día indicado. */
fun List<PatrolRoute>.forDay(day: DayOfWeek): List<PatrolRoute> =
    filter { route -> route.isActive && route.schedules.any { it.days.contains(day) } }
