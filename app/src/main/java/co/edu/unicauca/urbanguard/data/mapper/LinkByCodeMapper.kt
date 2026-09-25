package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoSearchResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestResponseDto
import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.model.UnitOption

fun ConjuntoSearchResponseDto.toDomain(): ConjuntoSearch = ConjuntoSearch(
    id = id,
    nombre = name,
    direccion = address,
    ciudad = city,
    totalMiembros = numberOfUnits,
    unidades = units.map { UnitOption(it.id, it.identifier) }
)

fun LinkRequestResponseDto.toDomain(): LinkRequest = LinkRequest(
    id = id,
    conjuntoId = conjuntoId,
    conjuntoNombre = complexName.orEmpty(),
    nombreSolicitante = applicantName.orEmpty(),
    emailSolicitante = applicantEmail.orEmpty(),
    fechaSolicitud = DateTimeUtils.formatFechaSolicitud(createdAt),
    unidad = unit,
    estado = status
)