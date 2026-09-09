package co.edu.unicauca.urbanguard.ui.features.onboarding

data class ActivateConjuntoUiState(
    val isLoading: Boolean = false,
    val code: String = "",
    val errorMessage: String? = null,
    val isValidated: Boolean = false,
    val conjuntoId: String? = null
)