package co.edu.unicauca.urbanguard.domain.repository

/**
 * Operaciones de usuario propias de identity-service.
 * El sub del JWT autenticado identifica al actor; no se aceptan IDs externos.
 */
interface UserRepository {

    /**
     * Solicita al backend un ticket de cambio de contraseña de Auth0.
     * El backend valida que:
     *  - El usuario exista y esté activo.
     *  - El token NO sea M2M.
     *  - El sub empiece por "auth0|" (cuenta nativa, no social/federada).
     *  - El auth_time del token sea menor a 5 minutos (frescura de sesión).
     *
     * @return URL del ticket (credencial temporal sensible) en caso de éxito,
     *         o un [AppError.ReauthRequired] si auth_time supera el umbral.
     */
    suspend fun createPasswordChangeTicket(): Result<String>
}
