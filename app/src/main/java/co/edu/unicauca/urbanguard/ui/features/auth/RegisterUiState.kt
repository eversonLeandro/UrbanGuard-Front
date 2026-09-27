package co.edu.unicauca.urbanguard.ui.features.auth

import co.edu.unicauca.urbanguard.domain.model.UserRole

/**
 * Estado del formulario de registro.
 *
 * Cada campo lleva su propio error para que la UI marque únicamente el campo
 * culpable. [errorMessage] se reserva para fallos de servidor (correo duplicado,
 * contraseña filtrada, sin conexión), que no pertenecen a ningún campo concreto.
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false,
    val userRole: UserRole? = null
) {
    val hasFieldErrors: Boolean
        get() = nameError != null || emailError != null || passwordError != null || confirmPasswordError != null
}
