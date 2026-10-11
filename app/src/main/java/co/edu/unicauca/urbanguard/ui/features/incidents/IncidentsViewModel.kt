package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.IncidentConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import co.edu.unicauca.urbanguard.domain.usecase.incidents.GetIncidentsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.incidents.RetryPendingIncidentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentsViewModel @Inject constructor(
    private val getIncidentsUseCase: GetIncidentsUseCase,
    private val retryPendingIncidentsUseCase: RetryPendingIncidentsUseCase,
    private val repository: IncidentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentsUiState(isLoading = true))
    val uiState: StateFlow<IncidentsUiState> = _uiState.asStateFlow()

    /**
     * La pantalla lo llama en cada ON_RESUME (entrada y vuelta del
     * formulario), así que no se carga en el `init`: se duplicaría.
     */
    fun onRefresh() {
        val complexId = activeComplexId() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            retryPendingIncidentsUseCase(complexId)
            getIncidentsUseCase(complexId).fold(
                onSuccess = { incidents ->
                    _uiState.value = IncidentsUiState(incidents = incidents)
                },
                onFailure = { error ->
                    _uiState.value = IncidentsUiState(
                        errorMessage = error.message ?: IncidentConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun selectIncident(incident: SecurityIncident) {
        repository.setSelectedIncident(incident)
    }

    private fun activeComplexId(): String? {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = IncidentsUiState(
                errorMessage = IncidentConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return null
        }
        return complexId
    }
}
