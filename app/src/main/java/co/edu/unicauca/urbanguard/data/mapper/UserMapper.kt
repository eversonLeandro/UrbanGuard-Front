package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.UserResponseDto
import co.edu.unicauca.urbanguard.domain.model.User

/**
 * Conversión DTO → dominio para el catálogo de usuarios del superadmin.
 * Sin reflexión ni librerías de mapeo: son dos campos derivables en total.
 */
fun UserResponseDto.toDomain(): User = User(
    publicId = publicId.orEmpty(),
    userId = userId,
    email = email,
    nombres = nombres,
    apellidos = apellidos,
    activo = activo,
    createdAt = createdAt,
    updatedAt = updatedAt
)
