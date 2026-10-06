package co.edu.unicauca.urbanguard.data.remote.auth0

import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ReauthLoginProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ReauthLoginProvider] real: delega al login embebido de Auth0.
 */
@Singleton
class Auth0ReauthLoginProvider @Inject constructor(
    private val auth0Manager: Auth0Manager
) : ReauthLoginProvider {

    override suspend fun login(email: String, password: String): UserSession =
        auth0Manager.loginEmbedded(email, password)
}
