package co.edu.unicauca.urbanguard.ui.features.auth

data class PasswordResetUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val errorMessage: String? = null,
    val requestAccepted: Boolean = false
)
