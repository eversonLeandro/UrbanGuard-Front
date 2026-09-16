package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoSearchResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestResponseDto
import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest

fun ConjuntoSearchResponseDto.toDomain(): ConjuntoSearch = ConjuntoSearch(
    id = id,
    nombre = nombre,
    direccion = direccion,
    ciudad = ciudad,
    totalMiembros = totalMiembros
)

fun LinkRequestResponseDto.toDomain(): LinkRequest = LinkRequest(
    conjuntoId = conjuntoId,
    fechaSolicitud = fechaSolicitud,
    unidad = unidad,
    estado = estado
)