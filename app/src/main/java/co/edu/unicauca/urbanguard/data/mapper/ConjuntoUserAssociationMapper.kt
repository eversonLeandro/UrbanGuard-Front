package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ComplexSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoUserAssociationDto
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole

/** Mapper para el DTO legacy (campos en español — flujos internos existentes). */
fun ConjuntoUserAssociationDto.toDomain(): ConjuntoUserAssociation = ConjuntoUserAssociation(
    conjuntoId = conjuntoId,
    nombre = nombre,
    roleEnConjunto = parseUserRole(roleEnConjunto)
)

/**
 * Mapper para la respuesta real del backend:
 * GET /api/complexes → [ComplexSummaryDto] → [ConjuntoUserAssociation].
 *
 * Solo se consumen [id], [name] y [role]; address/city/numberOfUnits/unit
 * se ignoran intencionalmente (el modelo de dominio no los expone en este cambio).
 */
fun ComplexSummaryDto.toDomain(): ConjuntoUserAssociation = ConjuntoUserAssociation(
    conjuntoId = id,
    nombre = name,
    roleEnConjunto = parseUserRole(role)
)

/**
 * Traduce el string de rol proveniente de Auth0 o del backend al enum [UserRole].
 *
 * Visible internamente en el módulo `data.mapper` para ser reutilizado por
 * ambas extensiones `toDomain()` sin duplicar la lógica.
 */
internal fun parseUserRole(rawRole: String?): UserRole {
    return when (rawRole?.trim()?.uppercase()) {
        "ADMIN", "ADMINISTRADOR" -> UserRole.ADMIN
        "GUARD", "VIGILANTE", "PORTERO", "SEGURIDAD" -> UserRole.GUARD
        "RESIDENT", "RESIDENTE", "PROPIETARIO", "INQUILINO" -> UserRole.RESIDENT
        "USER", "USUARIO" -> UserRole.USER
        else -> UserRole.RESIDENT
    }
}