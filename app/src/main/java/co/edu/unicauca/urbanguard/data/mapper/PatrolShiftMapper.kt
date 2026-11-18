package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardRefDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftIncidentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftSummaryDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardRef
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundReason
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftIncident
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftRound
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Mapeo del contrato del historial de patrullaje por turno.
 *
 * Los DTOs son la fuente de verdad. Ante un enum desconocido, una fecha/instante
 * inválido o un campo obligatorio ausente se **falla toda la respuesta** con
 * `AppException(AppError.Unexpected(MSG_INVALID_SERVER_RESPONSE))`: no se
 * descarta ningún elemento en silencio, porque una respuesta parcial es un error
 * del contrato y no debe mostrarse como un historial incompleto.
 *
 * Los campos opcionales por contrato (`goalMet`, `guard`, `scheduledAt`,
 * `startedAt`, `status`, `reason` de una ronda) siguen siendo nullables.
 */

fun PatrolShiftSummaryDto.toDomain(): PatrolShiftSummary {
    val type = PatrolShiftType.fromWire(required(shiftType)) ?: invalidResponse()
    val day = parseDate(required(date)) ?: invalidResponse()
    val state = PatrolShiftStatus.fromWire(required(status)) ?: invalidResponse()
    return PatrolShiftSummary(
        shiftType = type,
        date = day,
        status = state,
        expectedRounds = expectedRounds,
        completedRounds = completedRounds,
        pointsExpected = pointsExpected,
        pointsScanned = pointsScanned,
        goalMet = goalMet
    )
}

fun PatrolShiftDetailDto.toDomain(): PatrolShiftDetail {
    val state = PatrolShiftStatus.fromWire(required(status)) ?: invalidResponse()
    return PatrolShiftDetail(
        status = state,
        expectedRounds = expectedRounds,
        completedRounds = completedRounds,
        pointsExpected = pointsExpected,
        pointsScanned = pointsScanned,
        goalMet = goalMet,
        rounds = rounds.map { it.toDomain() }
    )
}

fun PatrolShiftRoundDto.toDomain(): PatrolShiftRound = PatrolShiftRound(
    routeName = required(routeName),
    guard = guard?.toDomain(),
    scheduledAt = parseInstant(scheduledAt),
    startedAt = parseInstant(startedAt),
    status = status?.let { PatrolRoundStatus.fromWire(it) ?: invalidResponse() },
    reason = reason?.let { PatrolRoundReason.fromWire(it) ?: invalidResponse() },
    pointsScanned = pointsScanned,
    pointsExpected = pointsExpected,
    occurrenceId = occurrenceId?.takeIf(String::isNotBlank),
    incidents = incidents.orEmpty().map { it.toDomain() }
)

private fun PatrolShiftIncidentDto.toDomain(): PatrolShiftIncident = PatrolShiftIncident(
    reportId = required(reportId),
    category = required(category),
    description = required(description),
    reportedAt = parseInstant(required(reportedAt)) ?: invalidResponse(),
    guardName = guardName?.takeIf(String::isNotBlank)
)

fun PatrolGuardRefDto.toDomain(): PatrolGuardRef =
    PatrolGuardRef(id = required(id), name = required(name))

fun PatrolGuardSummaryDto.toDomain(): PatrolGuardSummary = PatrolGuardSummary(
    guardName = required(guardName),
    roundsPerformed = roundsPerformed,
    shiftsWithParticipation = shiftsWithParticipation,
    averageRoundsPerShift = averageRoundsPerShift,
    routes = routes
)

/** Campo obligatorio presente y no en blanco, o falla la respuesta. */
private fun required(value: String?): String =
    value?.takeIf { it.isNotBlank() } ?: invalidResponse()

private fun parseDate(value: String): LocalDate =
    runCatching { LocalDate.parse(value) }.getOrNull() ?: invalidResponse()

/** Instante opcional: null si no viene; falla la respuesta si viene malformado. */
private fun parseInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(value).toInstant() }
        .recoverCatching { Instant.parse(value) }
        .getOrNull()
        ?: invalidResponse()
}

private fun invalidResponse(): Nothing =
    throw AppException(AppError.Unexpected(PatrolHistoryConstants.MSG_INVALID_SERVER_RESPONSE))
