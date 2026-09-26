package co.edu.unicauca.urbanguard.ui.features.auth

import co.edu.unicauca.urbanguard.domain.model.UserRole

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val userRole: UserRole? = null
)