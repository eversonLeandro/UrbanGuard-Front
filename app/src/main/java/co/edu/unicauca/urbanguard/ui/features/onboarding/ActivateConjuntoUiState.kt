package co.edu.unicauca.urbanguard.ui.features.onboarding

data class ActivateConjuntoUiState(
    val isLoading: Boolean = false,
    val code: String = "",
    val errorMessage: String? = null,
    val isValidated: Boolean = false,
    // Código ACT-XXXX-YYYY ingresado por el usuario, propagado a la
    // siguiente pantalla por navegación. No viene del backend (204 sin body).
    val activationCode: String? = null
)
