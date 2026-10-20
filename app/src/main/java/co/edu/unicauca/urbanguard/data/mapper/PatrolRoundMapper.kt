package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundPointDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScanDto
import co.edu.unicauca.urbanguard.domain.model.IncompleteReason
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolScan
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import java.time.Instant

/**
 * Mapeo de rondas y lecturas (mismo contrato que usará el backend).
 *
 * Cada ronda guarda la foto fija de puntos con la que se abrió; el total se
 * deriva de esa lista y no se persiste como contador independiente.
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

fun PatrolControlPoint.toRoundPointDto(): PatrolRoundPointDto = PatrolRoundPointDto(
    id = id,
    name = name,
    location = location,
    description = description,
    nfcTagId = nfcTagId,
    active = isActive
)

fun CheckpointResponseDto.toRoundPointDto(): PatrolRoundPointDto = PatrolRoundPointDto(
    id = checkpointId,
    name = name,
    location = location.orEmpty(),
    description = description,
    nfcTagId = normalizeNfcUid(currentTag?.uid.orEmpty()),
    active = active
)

fun PatrolRoundPointDto.toDomain(complexId: String): PatrolControlPoint =
    PatrolControlPoint(
        id = id,
        complexId = complexId,
        name = name,
        location = location,
        description = description.orEmpty(),
        nfcTagId = nfcTagId,
        isActive = active
    )

fun PatrolRound.toDto(): PatrolRoundDto = PatrolRoundDto(
    id = id,
    routeId = routeId,
    routeName = routeName,
    startedAt = startedAt.toString(),
    finishedAt = finishedAt?.toString(),
    scans = scans.map { it.toDto() },
    points = points.map { it.toRoundPointDto() },
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
    points = points.orEmpty().map { it.toDomain(complexId) },
    scheduledAt = scheduledAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    dueAt = dueAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    incompleteReason = incompleteReason?.let {
        runCatching { IncompleteReason.valueOf(it) }.getOrNull()
    }
)
