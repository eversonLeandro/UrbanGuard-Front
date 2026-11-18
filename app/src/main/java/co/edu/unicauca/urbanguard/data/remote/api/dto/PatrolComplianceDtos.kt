package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * DTOs del detalle de cumplimiento de una ronda (nivel 3 del historial):
 * `GET api/complexes/{complexId}/patrol-compliance/{occurrenceId}`.
 *
 * Igual que en [PatrolShiftDtos], los campos obligatorios se declaran nulables
 * para detectar una respuesta incompleta en el mapper y fallar en vez de
 * mostrar datos a medias. Los instantes vienen en ISO-8601.
 */

/**
 * Detalle de una ronda: horario, estado, cumplimiento y sus puntos NFC.
 *
 * [status] viaja como "SCHEDULED"/"IN_PROGRESS"/"COMPLETED"/"INCOMPLETE"/
 * "NOT_PERFORMED" y [compliance] como "COMPLIANT"/"PENDING"/"NON_COMPLIANT".
 */
data class PatrolRoundComplianceDetailDto(
    val occurrenceId: String? = null,
    val routeName: String? = null,
    val scheduledAt: String? = null,
    val earliestStartAt: String? = null,
    val deadlineAt: String? = null,
    val status: String? = null,
    val compliance: String? = null,
    val expectedCheckpoints: Int,
    val completedCheckpoints: Int,
    val guardId: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val checkpoints: List<PatrolRoundCheckpointDto> = emptyList()
)

/**
 * Un punto NFC del snapshot de la ronda. [scannedAt] null significa que el
 * punto NO se escaneó (omitido); nunca se infiere un escaneo.
 */
data class PatrolRoundCheckpointDto(
    val checkpointId: String? = null,
    val position: Int,
    val name: String? = null,
    val scannedAt: String? = null
)
