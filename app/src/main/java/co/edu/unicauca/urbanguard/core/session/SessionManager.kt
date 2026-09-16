package co.edu.unicauca.urbanguard.core.session

import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
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

    // TODO: reemplazar por llamada real al backend que retorna las asociaciones
    fun loadMockAssociations() {
        val current = _session.value ?: return
        val mocks = listOf(
            ConjuntoUserAssociation("mock-1", "Torres del Prado", UserRole.ADMIN),
            ConjuntoUserAssociation("mock-2", "Conjunto El Pinar", UserRole.RESIDENT)
        )
        _session.value = current.copy(
            conjuntosAsociados = mocks,
            conjuntoActivoId = null,
            role = UserRole.USER
        )
    }

    fun addConjunto(association: ConjuntoUserAssociation) {
        val current = _session.value ?: return
        _session.value = current.copy(
            conjuntosAsociados = current.conjuntosAsociados + association
        )
    }

    fun setActiveConjunto(conjuntoId: String) {
        val current = _session.value ?: return
        val assoc = current.conjuntosAsociados.find { it.conjuntoId == conjuntoId } ?: return
        _session.value = current.copy(
            conjuntoActivoId = conjuntoId,
            role = assoc.roleEnConjunto
        )
    }

    fun getActiveConjunto(): ConjuntoUserAssociation? {
        val s = _session.value ?: return null
        val id = s.conjuntoActivoId ?: return null
        return s.conjuntosAsociados.find { it.conjuntoId == id }
    }

    fun currentRole(): UserRole? = _session.value?.role
}