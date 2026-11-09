package co.edu.unicauca.urbanguard.ui.features.guard

import java.time.Instant

data class GuardHomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val conjuntoName: String = "",
    val profilePhotoUrl: String? = null,
    val profilePhotoExpiresAt: Instant? = null,
    /**
     * El guardia quedó sin ninguna membresía activa (p. ej. el admin lo
     * inactivó desde la gestión del conjunto). La pantalla de locked view
     * sustituye TODO el panel de vigilancia: solo queda cerrar sesión o
     * reintentar la carga; ninguna acción ni navegación queda disponible.
     */
    val hasNoAccess: Boolean = false
)
