package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateGuardRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateGuardResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.GuardMembershipDto
import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.domain.model.MembershipRole

/**
 * Construye el body de POST /api/complexes/{complexId}/guards a partir de los
 * campos del formulario.
 *
 * Los valores llegan ya recortados y validados por el ViewModel: el backend
 * rechaza con 400 cualquier campo en blanco, así que aquí no se vuelve a
 * decidir nada.
 */
fun toCreateGuardRequestDto(
    email: String,
    nombres: String,
    apellidos: String,
    telefono: String
): CreateGuardRequestDto = CreateGuardRequestDto(
    email = email,
    nombres = nombres,
    apellidos = apellidos,
    telefono = telefono
)

/**
 * Convierte la respuesta 201 del alta de vigilante en el modelo de dominio.
 *
 * El `role` que llega siempre es "GUARD"; el `runCatching` no es defensa contra
 * un valor raro del backend, sino el mismo criterio que usa ConjuntoActivationMapper
 * al traducir el rol de un conjunto recién creado: si el servidor llegara a
 * inventar otro rol, la pantalla se cae a GUARD en vez de propagar un fallo que
 * el usuario no puede corregir.
 */
fun CreateGuardResponseDto.toDomain(): CreatedGuard = CreatedGuard(
    membershipId = membershipId,
    userId = userId,
    email = email,
    nombres = nombres,
    role = runCatching { MembershipRole.valueOf(role) }.getOrNull() ?: MembershipRole.GUARD
)

/**
 * Convierte una fila del listado en dominio, o `null` si la fila no sirve.
 *
 * Devolver `null` (y no lanzar) es deliberado: el repositorio filtra con
 * `mapNotNull`, así que una fila corrupta u omitida por identity-service
 * descarta solo esa fila y el resto del listado sigue visible para el admin.
 *
 * El nombre se resuelve con reservas por la misma razón: si identity-service
 * no entregó nombres, se usa el email; si tampoco hay email, un rótulo
 * genérico que mantiene la fila visible (el estado e identidad interna
 * siguen presentes y las acciones de inactivar/reactivar siguen útiles).
 */
fun GuardMembershipDto.toDomainOrNull(): GuardMembership? {
    if (membershipId.isBlank() || userId.isBlank()) {
        return null
    }
    val fullName = listOfNotNull(nombres?.trim(), apellidos?.trim())
        .filter(String::isNotBlank)
        .joinToString(" ")
    return GuardMembership(
        membershipId = membershipId,
        userId = userId,
        email = email?.trim()?.takeIf(String::isNotBlank),
        fullName = fullName.ifBlank { email?.trim()?.ifBlank { null } ?: "Vigilante" },
        active = active
    )
}
