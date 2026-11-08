package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdateUnitResponseDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException

fun UpdateUnitResponseDto.toUpdatedIdentifier(): String {
    requiredUpdatedUnit(id)
    return requiredUpdatedUnit(identifier)
}

private fun requiredUpdatedUnit(value: String?): String =
    value?.takeIf(String::isNotBlank) ?: invalidUpdatedUnitResponse()

private fun invalidUpdatedUnitResponse(): Nothing =
    throw AppException(
        AppError.Unexpected("El servidor devolvió datos inválidos para el apartamento actualizado.")
    )
