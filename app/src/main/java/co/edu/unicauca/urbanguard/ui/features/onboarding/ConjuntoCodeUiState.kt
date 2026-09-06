package co.edu.unicauca.urbanguard.ui.features.onboarding

data class ConjuntoCodeUiState(
    // Estado de carga inicial (cuando la pantalla abre por primera vez)
    val isLoadingInitial: Boolean = true,

    // Estado de carga de acción (generar o regenerar)
    val isLoadingAction: Boolean = false,

    // null = aún no se ha generado código para este conjunto
    val code: String? = null,

    val isCodeActive: Boolean = false,

    // TODO: reemplazar este campo por el nombre que venga del ConjuntoConfig
    // una vez implementado UG-45/UG-46. Por ahora se carga desde la respuesta
    // del endpoint de código.
    val conjuntoName: String = "Torres del Prado", // <-- NOMBRE QUEMADO TEMPORAL

    val errorMessage: String? = null,
    val showRegenerateConfirm: Boolean = false,
    val copySuccess: Boolean = false
)
