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

/** Punto congelado en la ronda al momento de abrirla. */
data class PatrolRoundPointDto(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val description: String? = null,
    val nfcTagId: String = "",
    val active: Boolean = true
)

/**
 * Ronda de patrullaje (contrato futuro para UG-95).
 *
 * La ronda guarda los puntos que la ruta tenía al abrirse. El total nunca se
 * persiste aparte: se deriva del tamaño de [points].
 */
data class PatrolRoundDto(
    val id: String,
    val routeId: String,
    val routeName: String,
    val startedAt: String,
    val finishedAt: String? = null,
    val scans: List<PatrolScanDto> = emptyList(),
    val points: List<PatrolRoundPointDto>? = null,
    val scheduledAt: String? = null,
    val dueAt: String? = null,
    val incompleteReason: String? = null
)

/**
 * Cuerpo para abrir una ronda (contrato futuro).
 *
 * POST `api/security/complexes/{complexId}/patrol-rounds` → 201 con la ronda.
 */
data class StartRoundRequestDto(
    val routeId: String
)

/**
 * Cuerpo para registrar una lectura (contrato futuro).
 *
 * POST `api/security/complexes/{complexId}/patrol-rounds/{roundId}/scans` → 200.
 */
data class RegisterScanRequestDto(
    val pointId: String? = null,
    val tagId: String,
    val scannedAt: String
)
