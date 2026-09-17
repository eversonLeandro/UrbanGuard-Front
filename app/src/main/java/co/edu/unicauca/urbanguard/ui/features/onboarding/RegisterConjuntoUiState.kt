package co.edu.unicauca.urbanguard.ui.features.onboarding

data class RegisterConjuntoUiState(
    val errorMessage: String? = null,
    val isUpdateSuccess: Boolean = false,
    val isLoadingAction: Boolean = false
)
