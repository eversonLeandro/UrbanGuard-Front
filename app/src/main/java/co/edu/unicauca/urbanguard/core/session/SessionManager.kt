package co.edu.unicauca.urbanguard.core.session

import android.util.Log
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

    /**
     * Actualiza la lista de conjuntos asociados del usuario con los datos
     * reales devueltos por GET /api/complexes (Community Service).
     *
     * - No modifica [UserSession.role]: el rol de Auth0 se preserva y solo
     *   cambia al llamar [setActiveConjunto] (cuando el usuario elige un conjunto).
     * - Resetea [UserSession.conjuntoActivoId] a null porque la lista puede
     *   haber cambiado desde la última sesión.
     * - No hace nada si no hay sesión activa.
     */
    fun setAssociations(conjuntos: List<ConjuntoUserAssociation>) {
        Log.d("SessionMgr", "setAssociations: ${conjuntos.size} items, activeId=${_session.value?.conjuntoActivoId}")
        val current = _session.value ?: return
        _session.value = current.copy(
            conjuntosAsociados = conjuntos,
            conjuntoActivoId = null
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