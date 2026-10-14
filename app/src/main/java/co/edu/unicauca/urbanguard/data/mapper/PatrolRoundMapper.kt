package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScanDto
import co.edu.unicauca.urbanguard.domain.model.IncompleteReason
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolScan
import java.time.Instant

/**
 * Mapeo de rondas y lecturas (mismo contrato que usará el backend).
 *
 * Archivo propio para no superar el máximo de funciones de detekt en
 * `PatrolMapper.kt`.
 */

/** Instantes en ISO-8601 (`Instant.toString` / `Instant.parse`). */
fun PatrolScan.toDto(): PatrolScanDto = PatrolScanDto(
    pointId = pointId,
    tagId = tagId,
    scannedAt = scannedAt.toString(),
    known = known
)

fun PatrolScanDto.toDomain(): PatrolScan = PatrolScan(
    pointId = pointId,
    tagId = tagId,
    scannedAt = runCatching { Instant.parse(scannedAt) }.getOrDefault(Instant.EPOCH),
    known = known
)

fun PatrolRound.toDto(): PatrolRoundDto = PatrolRoundDto(
    id = id,
    routeId = routeId,
    routeName = routeName,
    startedAt = startedAt.toString(),
    finishedAt = finishedAt?.toString(),
    scans = scans.map { it.toDto() },
    totalPoints = totalPoints,
    scheduledAt = scheduledAt?.toString(),
    dueAt = dueAt?.toString(),
    incompleteReason = incompleteReason?.name
)

fun PatrolRoundDto.toDomain(complexId: String): PatrolRound = PatrolRound(
    id = id,
    complexId = complexId,
    routeId = routeId,
    routeName = routeName,
    startedAt = runCatching { Instant.parse(startedAt) }.getOrDefault(Instant.EPOCH),
    finishedAt = finishedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    scans = scans.map { it.toDomain() },
    totalPoints = totalPoints,
    scheduledAt = scheduledAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    dueAt = dueAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    incompleteReason = incompleteReason?.let {
        runCatching { IncompleteReason.valueOf(it) }.getOrNull()
    }
)
