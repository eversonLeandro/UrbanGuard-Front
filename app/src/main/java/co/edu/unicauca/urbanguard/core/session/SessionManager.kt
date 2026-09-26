package co.edu.unicauca.urbanguard.core.session

import android.util.Log
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado de sesion en memoria mas una copia cifrada del rol en disco.
 *
 * El estado en memoria vive solo mientras el proceso aguante: cuando Android
 * recrea la Activity (por ejemplo al cambiar a modo oscuro) se reconstruye desde
 * cero y el rol de conjunto se perdia. [rolePersistence] cierra ese hueco
 * escribiendo en almacenamiento seguro cada vez que el rol o el conjunto activo
 * cambian, de modo que [co.edu.unicauca.urbanguard.data.repository.AuthRepositoryImpl.restoreSession]
 * pueda reaplicarlo al volver.
 */
@Singleton
class SessionManager @Inject constructor(
    private val rolePersistence: RolePersistence
) {

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session

    var token: String? = null
        private set

    fun saveSession(userSession: UserSession) {
        token = userSession.token
        _session.value = userSession
        persist(userSession)
    }

    fun clearSession() {
        token = null
        _session.value = null
        rolePersistence.clear()
    }

    /** Copia cifrada del ultimo rol conocido, o null si todavia no hay ninguna. */
    fun readPersistedRole(): PersistedRole? = rolePersistence.read()

    private fun persist(userSession: UserSession) {
        rolePersistence.save(
            PersistedRole(
                userId = userSession.userId,
                role = userSession.role,
                conjuntoActivoId = userSession.conjuntoActivoId,
                conjuntoActivoNombre = userSession.conjuntoActivoNombre
            )
        )
    }

    /**
     * Actualiza la lista de conjuntos asociados del usuario con los datos
     * reales devueltos por GET /api/complexes (Community Service).
     *
     * - No modifica [UserSession.role]: el rol de Auth0 se preserva y solo
     *   cambia al llamar [setActiveConjunto] (cuando el usuario elige un conjunto).
     * - Conserva [UserSession.conjuntoActivoId] y [UserSession.conjuntoActivoNombre]
     *   si el conjunto activo sigue estando en la lista nueva, y los borra si ya no
     *   aparece (el usuario perdio la membresia). Antes se reseteaban siempre, lo que
     *   hacia que cualquier recarga de la lista dejara al usuario sin conjunto.
     * - No hace nada si no hay sesión activa.
     */
    fun setAssociations(conjuntos: List<ConjuntoUserAssociation>) {
        val current = _session.value ?: return
        val stillActive = current.conjuntoActivoId
            ?.let { activeId -> conjuntos.firstOrNull { it.conjuntoId == activeId } }
        Log.d(
            TAG,
            "setAssociations: ${conjuntos.size} items, activeId=${current.conjuntoActivoId} -> ${stillActive?.conjuntoId}"
        )
        val updated = current.copy(
            conjuntosAsociados = conjuntos,
            conjuntoActivoId = stillActive?.conjuntoId,
            conjuntoActivoNombre = stillActive?.nombre
        )
        _session.value = updated
        persist(updated)
    }

    fun addConjunto(association: ConjuntoUserAssociation) {
        val current = _session.value ?: return
        _session.value = current.copy(
            conjuntosAsociados = current.conjuntosAsociados + association
        )
    }

    /**
     * Marca [conjuntoId] como el conjunto activo y adopta el rol que el usuario tiene
     * en el. No hace nada si el conjunto no esta en la lista de asociaciones: aceptar
     * un id desconocido dejaria la sesion apuntando a un conjunto que el usuario ya no
     * tiene, asi que es preferible ignorar la operacion y dejarlo registrado.
     */
    fun setActiveConjunto(conjuntoId: String) {
        val current = _session.value ?: return
        val assoc = current.conjuntosAsociados.find { it.conjuntoId == conjuntoId }
        if (assoc == null) {
            Log.w(TAG, "setActiveConjunto: '$conjuntoId' no esta en las asociaciones, se ignora")
            return
        }
        val updated = current.copy(
            conjuntoActivoId = conjuntoId,
            conjuntoActivoNombre = assoc.nombre,
            role = assoc.roleEnConjunto
        )
        _session.value = updated
        persist(updated)
    }

    /**
     * Devuelve la asociacion del conjunto activo.
     *
     * Se resuelve primero contra la lista en memoria ([UserSession.conjuntosAsociados]),
     * que es la fuente autoritativa. Si todavia no se ha cargado (arranque en frio) pero
     * hay nombre persistido, se reconstruye la asociacion con ese nombre: asi la UI
     * muestra el conjunto real del usuario en vez de un valor por defecto inventado.
     */
    fun getActiveConjunto(): ConjuntoUserAssociation? {
        val s = _session.value ?: return null
        val id = s.conjuntoActivoId ?: return null
        s.conjuntosAsociados.find { it.conjuntoId == id }?.let { return it }
        val nombre = s.conjuntoActivoNombre?.takeIf { it.isNotBlank() } ?: return null
        return ConjuntoUserAssociation(
            conjuntoId = id,
            nombre = nombre,
            roleEnConjunto = s.role
        )
    }

    fun currentRole(): UserRole? = _session.value?.role

    private companion object {
        const val TAG = "SessionMgr"
    }
}
