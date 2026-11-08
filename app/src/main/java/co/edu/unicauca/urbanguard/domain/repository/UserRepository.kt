package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import okhttp3.MultipartBody

/**
 * Operaciones de usuario propias de identity-service.
 * El sub del JWT autenticado identifica al actor; no se aceptan IDs externos.
 */
interface UserRepository {

    suspend fun getOwnProfile(): Result<OwnProfile>

    suspend fun updateProfilePhoto(image: MultipartBody.Part, token: String): Result<OwnProfile>

    /** Estados del listado administrativo aceptados por el backend (`?estado=`). */
    companion object {
        const val ESTADO_ACTIVOS = "activos"
        const val ESTADO_INACTIVOS = "inactivos"
        const val ESTADO_TODOS = "todos"
    }

    /**
     * Lista las cuentas de la plataforma por estado. Solo llega con sesión de
     * SUPERADMIN; el backend responde 403 para cualquier otro rol.
     */
    suspend fun listUsers(estado: String = ESTADO_ACTIVOS): Result<List<User>>

    /**
     * Desactiva (soft delete) la cuenta de [userId]: el acceso se revoca en
     * Auth0 y la fila local se conserva con su historial. Idempotente; el 502
     * de un fallo persistente se reintenta con la misma llamada.
     *
     * La autodesactivación se impide en el servidor (409) y en la UI: la fila
     * propia no se ofrece para desactivar.
     */
    suspend fun deactivateUser(userId: String): Result<Unit>

    /**
     * Reactiva una cuenta desactivada y devuelve su estado actualizado.
     * Auth0 (fuente de verdad) se desbloquea siempre; el espejo local se
     * sincroniza después con `activo=true`.
     */
    suspend fun reactivateUser(userId: String): Result<User>

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
