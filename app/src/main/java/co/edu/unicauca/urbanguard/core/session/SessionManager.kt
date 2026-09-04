package co.edu.unicauca.urbanguard.core.session

import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session

    var token: String? = null
        private set

    fun saveSession(userSession: UserSession) {
        token = userSession.token
        _session.value = userSession
    }

    fun clearSession() {
        token = null
        _session.value = null
    }

    fun currentRole(): UserRole? = _session.value?.role
}