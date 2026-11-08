package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateUnitResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialResidentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialUnitDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentType
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit

fun ResidentialUnitDto.toDomain(): ResidentialUnit = ResidentialUnit(
    id = required(id),
    identifier = required(identifier),
    residents = (residents ?: invalidResponse()).map(ResidentialResidentDto::toDomain)
)

fun CreateUnitResponseDto.toDomain(): RegisteredResidentialUnit = RegisteredResidentialUnit(
    id = requiredUnit(id),
    identifier = requiredUnit(identifier),
    numberOfUnits = validUnitCount(numberOfUnits)
)

private fun ResidentialResidentDto.toDomain(): ResidentialResident = ResidentialResident(
    userId = userId?.takeIf(String::isNotBlank),
    fullName = required(fullName),
    type = residentType(type)
)

private fun residentType(value: String?): ResidentType? = when (value?.trim()?.uppercase()) {
    null, "" -> null
    ResidentType.OWNER.name -> ResidentType.OWNER
    ResidentType.TENANT.name -> ResidentType.TENANT
    else -> invalidResponse()
}

private fun required(value: String?): String =
    value?.takeIf(String::isNotBlank) ?: invalidResponse()

private fun requiredUnit(value: String?): String =
    value?.takeIf(String::isNotBlank) ?: invalidUnitResponse()

private fun validUnitCount(value: Int?): Int =
    value?.takeIf { it > 0 } ?: invalidUnitResponse()

private fun invalidResponse(): Nothing =
    throw AppException(AppError.Unexpected("El servidor devolvió datos inválidos para el censo."))

private fun invalidUnitResponse(): Nothing =
    throw AppException(
        AppError.Unexpected("El servidor devolvió datos inválidos para el nuevo apartamento.")
    )
