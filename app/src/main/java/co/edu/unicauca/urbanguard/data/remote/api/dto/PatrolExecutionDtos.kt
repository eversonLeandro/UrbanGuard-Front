package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Representación de una ocurrencia de patrullaje en el backend.
 * GET `/api/complexes/{complexId}/patrol-occurrences/available`
 */
data class PatrolOccurrenceDto(
    val occurrenceId: String,
    val routeId: String,
    val routeName: String,
    val scheduledAt: String,
    val earliestStartAt: String,
    val deadlineAt: String,
    val pointsExpected: Int,
    val status: String,
    val available: Boolean,
    val executionId: String? = null,
    val serverTime: String? = null
)

/**
 * Representación de un punto de control en la ejecución activa.
 */
data class PatrolPointViewDto(
    val checkpointId: String,
    val position: Int,
    val name: String,
    val scannedAt: String? = null
)

/**
 * Representación de una ejecución de patrullaje en el backend.
 * GET `/api/complexes/{complexId}/patrol-executions/active`
 * GET `/api/complexes/{complexId}/patrol-executions/{executionId}`
 * POST `/api/complexes/{complexId}/patrol-occurrences/{occurrenceId}/start`
 * POST `/api/complexes/{complexId}/patrol-executions/{executionId}/finish`
 */
data class PatrolExecutionDto(
    val executionId: String,
    val occurrenceId: String,
    val routeId: String,
    val routeName: String,
    val scheduledAt: String,
    val earliestStartAt: String,
    val deadlineAt: String,
    val startedAt: String,
    val completedAt: String? = null,
    val completionReason: String? = null,
    val status: String,
    val serverTime: String? = null,
    val pointsCompleted: Int,
    val pointsExpected: Int,
    val expectedCheckpoint: PatrolPointViewDto? = null,
    val checkpoints: List<PatrolPointViewDto> = emptyList()
)

/**
 * Solicitud de escaneo NFC de un punto.
 * POST `/api/complexes/{complexId}/patrol-executions/{executionId}/scans`
 */
data class CheckpointScanRequestDto(
    val uid: String
)

/**
 * Lote semanal de programación de rondas en el backend.
 * POST `/api/complexes/{complexId}/patrol-occurrences/batches`
 */
data class BatchOccurrenceEntryDto(
    val routeId: String,
    val scheduledAt: String
)

data class CreatePatrolBatchRequestDto(
    val weekStartDate: String,
    val occurrences: List<BatchOccurrenceEntryDto>,
    val routeIds: List<String>
)
