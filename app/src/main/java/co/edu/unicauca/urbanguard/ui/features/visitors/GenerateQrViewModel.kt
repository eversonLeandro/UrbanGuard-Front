package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.visitors.GenerateVisitorQrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Eventos de una sola entrega que la pantalla del formulario debe consumir. */
sealed interface GenerateQrEvent {
    /** El pase se creó correctamente; la pantalla debe ir a mostrarlo. */
    data object PassGenerated : GenerateQrEvent
}

@HiltViewModel
class GenerateQrViewModel @Inject constructor(
    private val generateVisitorQrUseCase: GenerateVisitorQrUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateQrUiState())
    val uiState: StateFlow<GenerateQrUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<GenerateQrEvent>(Channel.BUFFERED)
    val events: Flow<GenerateQrEvent> = eventChannel.receiveAsFlow()

    fun onVisitorNameChange(visitorName: String) {
        _uiState.value = _uiState.value.copy(visitorName = visitorName, errorMessage = null)
    }

    fun onGuestCountChange(guestCount: Int) {
        val clamped = guestCount.coerceIn(VisitorConstants.MIN_GUESTS, VisitorConstants.MAX_GUESTS)
        _uiState.value = _uiState.value.copy(guestCount = clamped, errorMessage = null)
    }

    fun onDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date, errorMessage = null)
    }

    /**
     * Valida el formulario y pide el pase. Ante exito se emite
     * [GenerateQrEvent.PassGenerated] y el formulario queda limpio para el
     * siguiente uso; el error, si lo hay, se muestra en la misma pantalla.
     */
    fun onGenerateClick() {
        val state = _uiState.value
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        val validationError = validate(state, complexId)
        if (validationError != null) {
            _uiState.value = state.copy(isLoading = false, errorMessage = validationError)
            return
        }
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            generateVisitorQrUseCase(
                visitorName = state.visitorName.trim(),
                guestCount = state.guestCount,
                date = state.date,
                complexId = complexId.orEmpty()
            ).fold(
                onSuccess = {
                    _uiState.value = GenerateQrUiState(date = state.date)
                    eventChannel.send(GenerateQrEvent.PassGenerated)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: VisitorConstants.MSG_GENERIC_ERROR
                    )
                }
            )
        }
    }

    /**
     * Devuelve el primer mensaje de validación que se incumpla, o null si el
     * formulario es válido. [complexId] llega null cuando el usuario no tiene
     * un conjunto activo en la sesión.
     */
    private fun validate(state: GenerateQrUiState, complexId: String?): String? {
        if (state.visitorName.isBlank()) return VisitorConstants.MSG_NAME_REQUIRED
        if (state.guestCount < VisitorConstants.MIN_GUESTS ||
            state.guestCount > VisitorConstants.MAX_GUESTS
        ) {
            return VisitorConstants.MSG_GUESTS_RANGE
        }
        if (state.date.isBefore(LocalDate.now())) return VisitorConstants.MSG_DATE_BEFORE_TODAY
        if (complexId.isNullOrBlank()) return VisitorConstants.MSG_NO_ACTIVE_COMPLEX
        return null
    }
}
