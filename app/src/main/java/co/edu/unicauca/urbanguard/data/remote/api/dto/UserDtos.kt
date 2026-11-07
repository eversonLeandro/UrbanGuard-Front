package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Espejo del record Java `UserResponse` de identity-service.
 *
 * Jackson del backend usa `non_null`, así que cualquier campo puede omitirse;
 * Gson los deja nulos sin error. [userId] es el `sub` de Auth0 (columna
 * `user_id`, nombre legacy), que es lo que la app usa para identificar la
 * fila propia del superadministrador ([co.edu.unicauca.urbanguard.domain.model.UserSession.userId]).
 * [publicId] es el UUID interno entre servicios y no se usa en la UI de este flujo.
 */
data class UserResponseDto(
    val publicId: String? = null,
    val userId: String,
    val email: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val telefono: String? = null,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
