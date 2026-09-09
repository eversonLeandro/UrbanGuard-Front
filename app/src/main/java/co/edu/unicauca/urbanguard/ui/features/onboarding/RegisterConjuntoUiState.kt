package co.edu.unicauca.urbanguard.ui.features.onboarding

data class RegisterConjuntoUiState(
    val isLoadingInitial: Boolean = true,
    val code: String? = null,
    val isCodeActive: Boolean = false,
    val errorMessage: String? = null,
    val copySuccess: Boolean = false,
    val isUpdateSuccess: Boolean = false,
    val isLoadingAction: Boolean = false
)
