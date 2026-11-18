package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundCheckpointDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundComplianceDetailDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PatrolComplianceLevel
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCheckpoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCompliance
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundExecutionStatus
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Mapeo del contrato del detalle de una ronda (nivel 3).
 *
 * Mismo criterio que [PatrolShiftMapper]: ante un enum desconocido, un
 * instante malformado o un campo obligatorio ausente se **falla toda la
 * respuesta** con `AppException(AppError.Unexpected(MSG_INVALID_SERVER_RESPONSE))`.
 * `scannedAt` es opcional por contrato (punto omitido) y no se inventa.
 */
fun PatrolRoundComplianceDetailDto.toDomain(): PatrolRoundCompliance {
    val state = PatrolRoundExecutionStatus.fromWire(required(status)) ?: invalidResponse()
    val level = PatrolComplianceLevel.fromWire(required(compliance)) ?: invalidResponse()
    return PatrolRoundCompliance(
        routeName = required(routeName),
        scheduledAt = parseInstant(required(scheduledAt)) ?: invalidResponse(),
        earliestStartAt = parseInstant(earliestStartAt),
        deadlineAt = parseInstant(deadlineAt),
        status = state,
        compliance = level,
        expectedCheckpoints = expectedCheckpoints,
        completedCheckpoints = completedCheckpoints,
        startedAt = parseInstant(startedAt),
        completedAt = parseInstant(completedAt),
        checkpoints = checkpoints.map { it.toDomain() }
    )
}

fun PatrolRoundCheckpointDto.toDomain(): PatrolRoundCheckpoint = PatrolRoundCheckpoint(
    position = position,
    name = required(name),
    scannedAt = parseInstant(scannedAt)
)

private fun required(value: String?): String =
    value?.takeIf { it.isNotBlank() } ?: invalidResponse()

private fun parseInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(value).toInstant() }
        .recoverCatching { Instant.parse(value) }
        .getOrNull()
        ?: invalidResponse()
}

private fun invalidResponse(): Nothing =
    throw AppException(AppError.Unexpected(PatrolHistoryConstants.MSG_INVALID_SERVER_RESPONSE))
