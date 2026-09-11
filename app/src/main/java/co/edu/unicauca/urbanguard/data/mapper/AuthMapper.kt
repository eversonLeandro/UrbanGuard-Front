package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.LoginResponseDto
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession

fun LoginResponseDto.toDomain(): UserSession = UserSession(
    token = token,
    userId = userId,
    name = name,
    role = UserRole.valueOf(role.uppercase()),
    hasConjunto = hasConjunto
)