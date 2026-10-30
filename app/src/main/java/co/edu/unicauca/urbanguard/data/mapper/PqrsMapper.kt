package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PqrsDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PqrsSummaryDto
import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.model.PqrsHistoryEntry
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsSummary
import co.edu.unicauca.urbanguard.domain.model.PqrsType

fun PqrsSummaryDto.toDomain(): PqrsSummary = PqrsSummary(
    id = id,
    type = PqrsType.fromString(type),
    subject = subject.ifBlank { "PQRS sin asunto" },
    description = description,
    status = PqrsStatus.fromString(status),
    authorId = authorId.orEmpty(),
    authorName = authorName?.takeIf { it.isNotBlank() } ?: "Residente",
    createdAt = createdAt.orEmpty(),
    updatedAt = updatedAt ?: createdAt.orEmpty(),
    adminResponse = adminResponse,
    respondedAt = respondedAt,
    resolvedAt = resolvedAt
)

fun PqrsDetailDto.toDomain(): PqrsDetail = PqrsDetail(
    id = id,
    type = PqrsType.fromString(type),
    subject = subject.ifBlank { "PQRS sin asunto" },
    description = description,
    status = PqrsStatus.fromString(status),
    authorId = authorId.orEmpty(),
    authorName = authorName?.takeIf { it.isNotBlank() } ?: "Residente",
    createdAt = createdAt.orEmpty(),
    updatedAt = updatedAt ?: createdAt.orEmpty(),
    adminResponse = adminResponse,
    respondedBy = respondedBy,
    respondedByName = respondedByName?.takeIf { it.isNotBlank() } ?: "Administrador",
    respondedAt = respondedAt,
    resolvedAt = resolvedAt,
    history = history.map { entry ->
        PqrsHistoryEntry(
            id = entry.id ?: "${entry.createdAt}-${entry.toStatus}",
            fromStatus = PqrsStatus.fromString(entry.fromStatus),
            toStatus = PqrsStatus.fromString(entry.toStatus),
            response = entry.response.orEmpty(),
            respondedBy = entry.respondedBy,
            respondedByName = entry.respondedByName?.takeIf { it.isNotBlank() }
                ?: "Administrador",
            createdAt = entry.createdAt.orEmpty()
        )
    }
)

