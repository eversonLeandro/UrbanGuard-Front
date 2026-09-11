package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestDto
import co.edu.unicauca.urbanguard.domain.model.LinkRequest

fun LinkRequestDto.toDomain(): LinkRequest = LinkRequest(
    id = id,
    nombreSolicitante = nombreSolicitante,
    emailSolicitante = emailSolicitante,
    conjuntoId = conjuntoId,
    conjuntoNombre = conjuntoNombre,
    fechaSolicitud = fechaSolicitud,
    codigoVinculacion = codigoVinculacion,
    unidad = unidad,
    estado = estado
)