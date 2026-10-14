package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * DTOs del historial de patrullaje por turno (HU-034 / HU-032).
 *
 * El backend resuelve turnos, cumplimiento y meta; estos DTOs son la fuente de
 * verdad del contrato. Los campos obligatorios se declaran nullables para poder
 * detectar una respuesta incompleta en el mapper (Gson deja `null` cuando el
 * campo no viene) y fallar en vez de mostrar datos a medias. `shiftType` viaja
 * como "DAY"/"NIGHT", `status` como "CLOSED"/"PENDING" y las fechas como
 * `yyyy-MM-dd`; los instantes vienen en ISO-8601 con offset.
 */

/**
 * Turno del listado: `GET api/complexes/{complexId}/patrol-shifts`.
 *
 * [goalMet] es null mientras el turno está pendiente. El orden de la lista es
 * el que entrega el backend.
 */
data class PatrolShiftSummaryDto(
    val shiftType: String? = null,
    val date: String? = null,
    val status: String? = null,
    val expectedRounds: Int,
    val completedRounds: Int,
    val pointsExpected: Int,
    val pointsScanned: Int,
    val goalMet: Boolean? = null
)

/**
 * Detalle de un turno:
 * `GET api/complexes/{complexId}/patrol-shifts/detail?shiftType&date`.
 *
 * No repite `shiftType` ni `date`: ambos ya viajan en la petición.
 */
data class PatrolShiftDetailDto(
    val status: String? = null,
    val expectedRounds: Int,
    val completedRounds: Int,
    val pointsExpected: Int,
    val pointsScanned: Int,
    val goalMet: Boolean? = null,
    val rounds: List<PatrolShiftRoundDto> = emptyList()
)

/**
 * Una ronda esperada dentro del detalle del turno.
 *
 * `scheduledAt == null` marca una ronda realizada NO programada: en ese caso
 * `status` y `reason` también vienen null y no cuenta en "X de Y" ni en la meta.
 * `reason` solo acompaña a `status == "INCOMPLETE"`; `startedAt == null` indica
 * que la ronda no se realizó.
 */
data class PatrolShiftRoundDto(
    val routeName: String? = null,
    val guard: PatrolGuardRefDto? = null,
    val scheduledAt: String? = null,
    val startedAt: String? = null,
    val status: String? = null,
    val reason: String? = null,
    val pointsScanned: Int,
    val pointsExpected: Int
)

/** Vigilante asociado a una ronda del detalle. */
data class PatrolGuardRefDto(
    val id: String? = null,
    val name: String? = null
)

/**
 * Resumen de un vigilante:
 * `GET api/complexes/{complexId}/patrol-guards/{guardId}/summary?from&to`.
 */
data class PatrolGuardSummaryDto(
    val guardName: String? = null,
    val roundsPerformed: Int,
    val shiftsWithParticipation: Int,
    val averageRoundsPerShift: Double,
    val routes: List<String> = emptyList()
)
