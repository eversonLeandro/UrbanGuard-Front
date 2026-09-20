package co.edu.unicauca.urbanguard.ui.features.onboarding

import java.time.Instant

data class ConjuntoCodeUiState(
    // Estado de carga inicial (cuando la pantalla abre por primera vez)
    val isLoadingInitial: Boolean = true,

    // Estado de carga de acción (generar o regenerar)
    val isLoadingAction: Boolean = false,

    // null = aún no se ha generado código para este conjunto
    val code: String? = null,

    // Instante de expiración del código; null = no hay código
    val expiresAt: Instant? = null,

    // TODO: reemplazar este campo por el nombre que venga del ConjuntoConfig
    // una vez implementado UG-45/UG-46.
    val conjuntoName: String = "",

    val errorMessage: String? = null,
    val showRegenerateConfirm: Boolean = false,
    val copySuccess: Boolean = false
) {
    /** El código está activo si existe y no ha expirado */
    val isCodeActive: Boolean
        get() = expiresAt != null && Instant.now().isBefore(expiresAt)
}
