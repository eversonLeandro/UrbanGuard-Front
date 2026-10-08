package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure
import co.edu.unicauca.urbanguard.domain.model.toValidationFailure
import co.edu.unicauca.urbanguard.domain.model.validationDisplayMessage
import co.edu.unicauca.urbanguard.domain.usecase.visitors.ValidateVisitorQrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Valida el código QR escaneado por el guardia.
 *
 * - Resuelve el `complexId` desde el conjunto activo de la sesión (el mismo
 *   que emitió la invitación; el backend lo exige en la ruta).
 * - Ignora llamadas duplicadas mientras hay una validación en curso.
 * - Expone el resultado para que [GuardScanQrScreen] pinte la tarjeta
 *   correspondiente (autorizado / usado / expirado / inválido).
 */
@HiltViewModel
class GuardScanQrViewModel @Inject constructor(
    private val validateVisitorQrUseCase: ValidateVisitorQrUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardScanQrUiState())
    val uiState: StateFlow<GuardScanQrUiState> = _uiState.asStateFlow()

    private var validationJob: Job? = null

    fun validate(code: String) {
        if (_uiState.value.isLoading) return
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = GuardScanQrUiState(
                failure = VisitorValidationFailure.NO_ACTIVE_COMPLEX,
                errorMessage = "No tienes un conjunto activo seleccionado."
            )
            return
        }
        _uiState.value = GuardScanQrUiState(isLoading = true)
        validationJob?.cancel()
        validationJob = viewModelScope.launch {
            validateVisitorQrUseCase(code.trim(), complexId).fold(
                onSuccess = { result ->
                    _uiState.value = GuardScanQrUiState(result = result)
                },
                onFailure = { error ->
                    _uiState.value = GuardScanQrUiState(
                        failure = error.toValidationFailure(),
                        errorMessage = error.validationDisplayMessage(
                            "No se pudo validar el pase. Intenta de nuevo."
                        )
                    )
                }
            )
        }
    }

    fun reset() {
        validationJob?.cancel()
        _uiState.value = GuardScanQrUiState()
    }
}
