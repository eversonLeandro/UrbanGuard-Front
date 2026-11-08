package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateOwnerTenantRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.OwnerTenantResponseDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.ResidentType

fun OwnerTenantDraft.toRequestDto(): CreateOwnerTenantRequestDto =
    CreateOwnerTenantRequestDto(
        type = type.name,
        nombres = nombres,
        apellidos = apellidos,
        documento = documento,
        email = email,
        telefono = telefono
    )

fun OwnerTenantResponseDto.toDomain(): OwnerTenant = OwnerTenant(
    id = requiredResident(id),
    unitId = requiredResident(unitId),
    unitIdentifier = requiredResident(unitIdentifier),
    type = requiredResident(type).toResidentType(),
    fullName = requiredResident(fullName)
)

private fun requiredResident(value: String?): String =
    value?.takeIf(String::isNotBlank) ?: invalidResidentResponse()

private fun String.toResidentType(): ResidentType = when (uppercase()) {
    ResidentType.OWNER.name -> ResidentType.OWNER
    ResidentType.TENANT.name -> ResidentType.TENANT
    else -> invalidResidentResponse()
}

private fun invalidResidentResponse(): Nothing =
    throw AppException(
        AppError.Unexpected("El servidor devolvió datos inválidos para el residente.")
    )
