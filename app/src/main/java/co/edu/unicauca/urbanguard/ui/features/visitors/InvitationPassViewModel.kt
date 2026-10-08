package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import co.edu.unicauca.urbanguard.domain.usecase.visitors.GetInvitationCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Recupera el código de la invitación elegida en el listado para volver a
 * compartirla.
 *
 * La invitación llega por memoria del repositorio (la pone el listado al
 * pulsar); si no hay ninguna (p. ej. recreación del proceso) se muestra el
 * estado de error para volver atrás, igual que `GeneratedPassScreen`.
 */
@HiltViewModel
class InvitationPassViewModel @Inject constructor(
    private val getInvitationCodeUseCase: GetInvitationCodeUseCase,
    private val repository: VisitorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationPassUiState(isLoading = true))
    val uiState: StateFlow<InvitationPassUiState> = _uiState.asStateFlow()

    init {
        loadCode()
    }

    fun onRetry() {
        loadCode()
    }

    private fun loadCode() {
        val invitation = repository.selectedInvitation()
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (invitation == null || complexId.isNullOrBlank()) {
            _uiState.value = InvitationPassUiState(
                errorMessage = "No se encontró la invitación. Vuelve al listado."
            )
            return
        }
        _uiState.value = InvitationPassUiState(isLoading = true)
        viewModelScope.launch {
            getInvitationCodeUseCase(complexId, invitation.invitationId).fold(
                onSuccess = { code ->
                    _uiState.value = InvitationPassUiState(
                        pass = VisitorAuthorization(
                            qrCode = code,
                            validDate = invitation.visitDate,
                            visitorName = invitation.visitorName,
                            guestCount = invitation.guestCount
                        )
                    )
                },
                onFailure = { error ->
                    _uiState.value = InvitationPassUiState(
                        errorMessage = error.message
                            ?: "No se pudo recuperar el código."
                    )
                }
            )
        }
    }
}
