package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.UserSession

/**
 * Re-autenticación interactiva para operaciones sensibles (cambio de contraseña).
 *
 * Abstrae el login embebido de Auth0 para que el [ProfileViewModel] no dependa
 * de la clase concreta de infraestructura y sea unit-testeable sin mocks.
 */
interface ReauthLoginProvider {

    /**
     * Autentica con email y contraseña para renovar el `auth_time` del token.
     * Retorna la nueva sesión o lanza [co.edu.unicauca.urbanguard.domain.model.AppException].
     */
    suspend fun login(email: String, password: String): UserSession
}
