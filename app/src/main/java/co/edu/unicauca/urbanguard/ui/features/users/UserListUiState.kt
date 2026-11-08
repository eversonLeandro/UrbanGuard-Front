package co.edu.unicauca.urbanguard.ui.features.users

import co.edu.unicauca.urbanguard.domain.model.User

/**
 * Filtro de estado del listado. Cada valor se traduce directo al parámetro
 * `?estado=` del backend (ver [co.edu.unicauca.urbanguard.domain.repository.UserRepository]).
 */
enum class UserListFilter(val apiValue: String) {
    ACTIVE("activos"),
    INACTIVE("inactivos")
}

/**
 * Estado de la pantalla de gestión de cuentas del superadmin (HU-006).
 *
 * La lista cargada corresponde siempre al filtro activo: al cambiar de
 * filtro se recarga desde el servidor. La búsqueda de [query] es local sobre
 * lo ya cargado, porque el catálogo completo ya está en pantalla.
 */
data class UserListUiState(
    val filter: UserListFilter = UserListFilter.ACTIVE,
    val users: List<User> = emptyList(),

    /** Carga inicial del filtro: lista vacía + spinner de pantalla completa. */
    val isLoading: Boolean = false,

    /** Recarga del mismo filtro con datos ya visibles (pull/acción completada). */
    val isRefreshing: Boolean = false,

    val errorMessage: String? = null,

    /** Búsqueda local por nombre o email; vacía significa mostrar todas. */
    val query: String = "",

    /** `sub` de Auth0 del actor: su fila no se puede desactivar (409 del backend). */
    val myUserId: String? = null,

    /** `sub` de la cuenta con desactivar/reactivar en vuelo, para el estado de fila. */
    val actionInProgressUserId: String? = null,

    /** Cuenta mostrada en el diálogo de confirmación de desactivación, si la hay. */
    val pendingDeactivation: User? = null,

    /** Mensaje efímero de éxito (snackbar) tras desactivar o reactivar. */
    val successMessage: String? = null
) {

    /** Búsqueda insensible a mayúsculas y acentos del catálogo cargado. */
    val filteredUsers: List<User>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return users
            val needle = normalized(q)
            return users.filter { user ->
                normalized(user.displayName).contains(needle) ||
                    normalized(user.email.orEmpty()).contains(needle)
            }
        }

    private companion object {
        fun normalized(value: String): String = value
            .trim()
            .lowercase()
            .replace(Regex("[áàä]"), "a")
            .replace(Regex("[éèë]"), "e")
            .replace(Regex("[íìï]"), "i")
            .replace(Regex("[óòö]"), "o")
            .replace(Regex("[úùü]"), "u")
    }
}
