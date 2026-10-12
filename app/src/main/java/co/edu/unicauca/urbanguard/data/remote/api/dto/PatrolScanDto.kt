package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Una lectura de tag dentro de una ronda (UG-41 / UG-95).
 *
 * `scannedAt` en ISO-8601 instantáneo; `pointId` null cuando el tag no
 * pertenece a la ruta (`known = false`).
 */
data class PatrolScanDto(
    val pointId: String? = null,
    val tagId: String,
    val scannedAt: String,
    val known: Boolean
)

/**
 * Ronda de patrullaje (contrato propuesto para UG-95).
 *
 * GET `api/complexes/{complexId}/patrol-rounds` → 200 OK con la lista.
 */
data class PatrolRoundDto(
    val id: String,
    val routeId: String,
    val routeName: String,
    val startedAt: String,
    val finishedAt: String? = null,
    val scans: List<PatrolScanDto> = emptyList(),
    val totalPoints: Int = 0
)

/**
 * Cuerpo para abrir una ronda (contrato propuesto).
 *
 * POST `api/complexes/{complexId}/patrol-rounds` → 201 con la ronda.
 */
data class StartRoundRequestDto(
    val routeId: String
)

/**
 * Cuerpo para registrar una lectura (contrato propuesto).
 *
 * POST `api/complexes/{complexId}/patrol-rounds/{roundId}/scans` → 200.
 */
data class RegisterScanRequestDto(
    val pointId: String? = null,
    val tagId: String,
    val scannedAt: String
)
