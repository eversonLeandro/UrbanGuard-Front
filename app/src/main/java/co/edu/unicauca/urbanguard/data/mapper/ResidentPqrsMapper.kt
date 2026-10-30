package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsCreatedDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsHistoryEntryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsSummaryDto
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsHistoryEntry
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary

fun ResidentPqrsSummaryDto.toResidentDomain(): ResidentPqrsSummary = ResidentPqrsSummary(
    id = id,
    type = PqrsType.valueOf(type),
    subject = subject,
    status = PqrsStatus.valueOf(status),
    createdAt = createdAt
)

fun ResidentPqrsHistoryEntryDto.toResidentDomain(): ResidentPqrsHistoryEntry = ResidentPqrsHistoryEntry(
    id = id,
    fromStatus = PqrsStatus.valueOf(fromStatus),
    toStatus = PqrsStatus.valueOf(toStatus),
    response = response,
    createdAt = createdAt
)

fun ResidentPqrsDetailDto.toResidentDomain(): ResidentPqrsDetail = ResidentPqrsDetail(
    id = id,
    type = PqrsType.valueOf(type),
    subject = subject,
    description = description,
    status = PqrsStatus.valueOf(status),
    createdAt = createdAt,
    history = requireNotNull(history) { "Historial de PQRS no disponible" }
        .map { it.toResidentDomain() }
)

fun ResidentPqrsCreatedDto.toResidentDomain(): ResidentPqrsDetail = ResidentPqrsDetail(
    id = id,
    type = PqrsType.valueOf(type),
    subject = subject,
    description = description,
    status = PqrsStatus.valueOf(status),
    createdAt = createdAt
)
