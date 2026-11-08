package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.usecase.auth.RequestPasswordResetUseCase
import co.edu.unicauca.urbanguard.domain.validation.InputValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val requestPasswordReset: RequestPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            errorMessage = null
        )
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isLoading || state.requestAccepted) return

        if (!InputValidators.isValidEmail(state.email)) {
            _uiState.value = state.copy(emailError = "Ingresa un correo electrónico válido.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, emailError = null)
        viewModelScope.launch {
            requestPasswordReset(state.email.trim()).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        requestAccepted = true
                    )
                },
                onFailure = { error ->
                    val appError = AppError.from(error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = when (appError) {
                            is AppError.NoConnection ->
                                "No hay conexión a internet. Revisa tu red e intenta de nuevo."
                            is AppError.Timeout ->
                                "La solicitud tardó demasiado. Intenta de nuevo."
                            else -> appError.message
                        }
                    )
                }
            )
        }
    }
}
