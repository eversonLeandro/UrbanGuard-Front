package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialResidentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialUnitDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit

fun ResidentialUnitDto.toDomain(): ResidentialUnit = ResidentialUnit(
    id = required(id),
    identifier = required(identifier),
    residents = (residents ?: invalidResponse()).map(ResidentialResidentDto::toDomain)
)

private fun ResidentialResidentDto.toDomain(): ResidentialResident = ResidentialResident(
    userId = required(userId),
    fullName = required(fullName)
)

private fun required(value: String?): String =
    value?.takeIf(String::isNotBlank) ?: invalidResponse()

private fun invalidResponse(): Nothing =
    throw AppException(AppError.Unexpected("El servidor devolvió datos inválidos para el censo."))
