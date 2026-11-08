package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun signUp(email: String, password: String, name: String = ""): Result<UserSession>
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /**
     * Restaura la sesión persistida (si existe) al reabrir la app.
     * Devuelve null cuando no hay credenciales válidas o renovables.
     */
    suspend fun restoreSession(): UserSession?
    suspend fun logout()
}

