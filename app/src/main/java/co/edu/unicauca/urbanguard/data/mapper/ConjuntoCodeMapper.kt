package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoCodeResponseDto
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode

fun ConjuntoCodeResponseDto.toDomain(): ConjuntoCode = ConjuntoCode(
    code = code,
    isActive = isActive,
    conjuntoName = conjuntoName // TODO: remover cuando venga del ConjuntoConfig
)