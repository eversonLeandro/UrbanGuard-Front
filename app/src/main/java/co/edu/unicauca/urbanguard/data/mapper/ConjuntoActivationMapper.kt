package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateComplexResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterConjuntoRequestDto
import co.edu.unicauca.urbanguard.domain.model.CreatedComplex
import co.edu.unicauca.urbanguard.domain.model.MembershipRole

/**
 * Construye el body de POST /api/complexes partiendo de los campos del
 * formulario de registro.
 *
 * totalUnidades NO se mapea: el backend rechaza campos desconocidos con 400.
 * El campo se conserva únicamente en el estado local del formulario (UI)
 * para cuando el backend lo soporte.
 */
fun toRegisterConjuntoRequestDto(
    activationCode: String,
    name: String,
    address: String,
    city: String
): RegisterConjuntoRequestDto = RegisterConjuntoRequestDto(
    activationCode = activationCode,
    name = name,
    address = address,
    city = city
)

/**
 * Convierte la respuesta del endpoint POST /api/complexes (201 Created)
 * en el modelo interno de dominio.
 */
fun CreateComplexResponseDto.toDomain(): CreatedComplex = CreatedComplex(
    complexId = complexId,
    membershipId = membershipId,
    roles = roles.mapNotNull { name ->
        runCatching { MembershipRole.valueOf(name) }.getOrNull()
    }
)