package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.AppError

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.ActivateConjuntoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivateConjuntoViewModel @Inject constructor(
    private val activateConjuntoUseCase: ActivateConjuntoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivateConjuntoUiState())
    val uiState: StateFlow<ActivateConjuntoUiState> = _uiState.asStateFlow()

    fun onCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(code = code, errorMessage = null)
    }

    fun validateCode() {
        val code = _uiState.value.code
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Ingresa el código de activación."
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            activateConjuntoUseCase(code).fold(
                onSuccess = {
                    // Éxito (204): guardamos el código ingresado para llevarlo
                    // a la siguiente pantalla. No hay valor de retorno del backend.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isValidated = true,
                        activationCode = code
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isValidated = false,
                        activationCode = null,
                        errorMessage = AppError.from(error).message ?: "Ocurrió un error al validar el código."
                    )
                }
            )
        }
    }
}