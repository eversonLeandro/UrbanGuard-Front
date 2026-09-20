package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestDto
import co.edu.unicauca.urbanguard.domain.model.LinkRequest

fun LinkRequestDto.toDomain(): LinkRequest = LinkRequest(
    id = id,
    nombreSolicitante = applicantName?.takeIf { it.isNotBlank() } ?: userId,
    emailSolicitante = applicantEmail.orEmpty(),
    conjuntoId = conjuntoId,
    conjuntoNombre = complexName.orEmpty(),
    fechaSolicitud = createdAt.orEmpty(),
    unidad = unit,
    estado = status,
    roleSyncPending = roleSyncPending,
    organizationSyncPending = organizationSyncPending
)