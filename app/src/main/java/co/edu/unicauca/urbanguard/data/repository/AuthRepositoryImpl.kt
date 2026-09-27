package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.data.remote.auth0.Auth0Manager
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth0Manager: Auth0Manager,
    private val sessionManager: SessionManager
) : AuthRepository {

    /**
     * No usa [apiCall] porque el `catch` hace una limpieza de sesion antes de
     * convertir el error, y eso no se puede expresar en el bloque. El catch amplio
     * conserva la justificacion de [apiCall].
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val session = auth0Manager.loginEmbedded(email, password)
            sessionManager.saveSession(session)
            Result.success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Si el intento falla no se conserva ninguna sesión previa: así el usuario
            // no queda con la sesión de otra cuenta mientras cree haber entrado.
            sessionManager.clearSession()
            auth0Manager.clearStoredCredentials()
            Result.failure(toAppException(e))
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): Result<UserSession> {
        return try {
            val session = auth0Manager.signUpEmbedded(email, password, name)
            sessionManager.saveSession(session)
            Result.success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(toAppException(e))
        }
    }

    /**
     * Login y registro fallan por el mismo camino de errores: [Auth0Manager] ya
     * devuelve un [AppException] con el mensaje en español, y el mapper lo deja
     * intacto. Así ambos casos muestran exactamente el mismo texto y nunca se
     * anteponen genéricos del tipo "Ocurrió un error inesperado: ...".
     */
    private fun toAppException(e: Exception): AppException = when (e) {
        is AppException -> e
        else -> AppException(NetworkErrorMapper.toAppError(e))
    }

    override suspend fun restoreSession(): UserSession? {
        val restored = auth0Manager.restoreSession() ?: return null
        val merged = restored.withPersistedRole()
        sessionManager.saveSession(merged)
        return merged
    }

    override suspend fun logout() {
        auth0Manager.clearStoredCredentials()
        sessionManager.clearSession()
    }

    /**
     * Reaplica el rol de conjunto guardado en disco sobre la sesion restaurada.
     *
     * Auth0 solo conoce el claim de plataforma, asi que al reconstruir la sesion
     * un administrador de conjunto vuelve como RESIDENT/USER y el NavGraph lo
     * manda a la barra del residente. La copia cifrada de [SessionManager] es la
     * fuente de verdad para el rol de conjunto, ya que nunca viaja en el token.
     *
     * Se ignora si la copia pertenece a otro usuario, y el claim de plataforma
     * siempre manda: un SUPERADMIN autenticado por Auth0 nunca se degrada.
     */
    private fun UserSession.withPersistedRole(): UserSession {
        val persisted = sessionManager.readPersistedRole() ?: return this
        if (persisted.userId != userId) return this
        if (role == UserRole.SUPERADMIN) return this
        return copy(
            role = persisted.role,
            conjuntoActivoId = persisted.conjuntoActivoId,
            conjuntoActivoNombre = persisted.conjuntoActivoNombre
        )
    }
}
