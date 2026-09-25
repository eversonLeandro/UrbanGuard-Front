package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.data.remote.auth0.Auth0Manager
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth0Manager: Auth0Manager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val session = auth0Manager.loginEmbedded(email, password)
            sessionManager.saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            // Limpiamos la sesión anterior por si el nuevo intento falla
            sessionManager.clearSession()
            auth0Manager.clearStoredCredentials()
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<UserSession> {
        return try {
            val session = auth0Manager.signUpEmbedded(email, password, name)
            sessionManager.saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreSession(): UserSession? {
        val restored = auth0Manager.restoreSession() ?: return null
        sessionManager.saveSession(restored)
        return restored
    }

    override suspend fun logout() {
        auth0Manager.clearStoredCredentials()
        sessionManager.clearSession()
    }
}
