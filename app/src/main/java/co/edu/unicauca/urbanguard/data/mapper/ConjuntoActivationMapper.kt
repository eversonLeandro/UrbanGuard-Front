package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateComplexRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateComplexResponseDto
import co.edu.unicauca.urbanguard.domain.model.CreatedComplex
import co.edu.unicauca.urbanguard.domain.model.MembershipRole

/**
 * Construye el body de POST /api/complexes partiendo de los campos del
 * formulario de registro.
 *
 * Incluye numberOfUnits (requerido por backend @NotNull @Positive).
 */
fun toCreateComplexRequestDto(
    activationCode: String,
    name: String,
    address: String,
    city: String,
    numberOfUnits: Int
): CreateComplexRequestDto = CreateComplexRequestDto(
    activationCode = activationCode,
    name = name,
    address = address,
    city = city,
    numberOfUnits = numberOfUnits
)

/**
 * Convierte la respuesta del endpoint POST /api/complexes (201 Created)
 * en el modelo interno de dominio.
 *
 * El backend devuelve un solo role (MembershipRole), se envuelve en lista
 * para compatibilidad con el modelo de dominio CreatedComplex.
 */
fun CreateComplexResponseDto.toDomain(): CreatedComplex = CreatedComplex(
    complexId = complexId,
    membershipId = membershipId,
    roles = listOf(runCatching { MembershipRole.valueOf(role) }.getOrNull() ?: MembershipRole.ADMIN)
)
