package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.IncidentSyncState
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import java.time.Instant

/**
 * Mapeo de novedades (mismo contrato que usará el backend).
 *
 * En local `photoPath` es la ruta del archivo; del backend llega `photoUrl`.
 * Al mapear respuesta remota se conserva la ruta local si ya existía.
 */
fun IncidentResponseDto.toDomain(
    complexId: String,
    localPhotoPath: String? = null
): SecurityIncident =
    SecurityIncident(
        id = id,
        complexId = complexId,
        category = incidentCategoryOf(category),
        description = description,
        photoPath = localPhotoPath ?: photoUrl,
        pointId = pointId,
        pointName = pointName,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.EPOCH),
        syncState = if (synced) IncidentSyncState.SYNCED else IncidentSyncState.PENDING
    )

fun SecurityIncident.toResponseDto(): IncidentResponseDto = IncidentResponseDto(
    id = id,
    category = category.name,
    description = description,
    photoUrl = photoPath,
    pointId = pointId,
    pointName = pointName,
    createdAt = createdAt.toString(),
    synced = syncState == IncidentSyncState.SYNCED
)

fun incidentCategoryOf(raw: String?): IncidentCategory =
    runCatching { IncidentCategory.valueOf(raw?.trim()?.uppercase().orEmpty()) }
        .getOrDefault(IncidentCategory.OTHER)
