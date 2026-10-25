package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import java.time.Instant

fun IncidentResponseDto.toDomain(imageUrl: String? = null): SecurityIncident =
    SecurityIncident(
        id = reportId,
        complexId = complexId,
        category = incidentCategoryOf(category),
        description = description,
        imageUrl = imageUrl,
        hasImage = hasImage,
        pointId = checkpointId,
        guardId = guardId,
        executionId = executionId,
        createdAt = runCatching { Instant.parse(reportedAt) }.getOrDefault(Instant.EPOCH)
    )

fun incidentCategoryOf(raw: String?): IncidentCategory =
    runCatching { IncidentCategory.valueOf(raw?.trim()?.uppercase().orEmpty()) }
        .getOrDefault(IncidentCategory.OTHER)
