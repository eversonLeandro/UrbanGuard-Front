package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoUserAssociationDto
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole

fun ConjuntoUserAssociationDto.toDomain(): ConjuntoUserAssociation = ConjuntoUserAssociation(
    conjuntoId = conjuntoId,
    nombre = nombre,
    roleEnConjunto = parseUserRole(roleEnConjunto)
)

private fun parseUserRole(rawRole: String?): UserRole {
    return when (rawRole?.trim()?.uppercase()) {
        "ADMIN", "ADMINISTRADOR" -> UserRole.ADMIN
        "GUARD", "VIGILANTE", "PORTERO", "SEGURIDAD" -> UserRole.GUARD
        "RESIDENT", "RESIDENTE", "PROPIETARIO", "INQUILINO" -> UserRole.RESIDENT
        "USER", "USUARIO" -> UserRole.USER
        else -> UserRole.RESIDENT
    }
}