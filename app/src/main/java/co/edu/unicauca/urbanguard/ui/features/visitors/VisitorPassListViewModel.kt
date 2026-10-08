package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import co.edu.unicauca.urbanguard.domain.usecase.visitors.GetVisitorInvitationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class VisitorPassListViewModel @Inject constructor(
    private val getVisitorInvitationsUseCase: GetVisitorInvitationsUseCase,
    private val sessionManager: SessionManager,
    private val repository: VisitorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitorPassListUiState())
    val uiState: StateFlow<VisitorPassListUiState> = _uiState.asStateFlow()

    init {
        onRefresh()
    }

    /**
     * Vuelve a pedir el historial del conjunto activo. Se llama al entrar a
     * la pantalla para que el pase generado en el formulario aparezca al
     * volver al listado.
     */
    fun onRefresh() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = VisitorConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getVisitorInvitationsUseCase(complexId).fold(
                onSuccess = { passes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allPasses = passes,
                        filteredPasses = applyFilters(passes, _uiState.value.searchQuery, _uiState.value.dateFilter)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: VisitorConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            searchQuery = query,
            filteredPasses = applyFilters(state.allPasses, query, state.dateFilter)
        )
    }

    fun onDateFilterChange(date: LocalDate?) {
        val state = _uiState.value
        _uiState.value = state.copy(
            dateFilter = date,
            filteredPasses = applyFilters(state.allPasses, state.searchQuery, date)
        )
    }

    private fun applyFilters(
        passes: List<VisitorInvitation>,
        query: String,
        date: LocalDate?
    ): List<VisitorInvitation> {
        return passes.filter { pass ->
            val matchesName = if (query.isNotBlank()) pass.visitorName.contains(query, ignoreCase = true) else true
            val matchesDate = if (date != null) pass.visitDate == date else true
            matchesName && matchesDate
        }
    }

    /**
     * Guarda la invitación elegida para que la pantalla de recompartir la
     * recupere sin argumentos de navegación.
     */
    fun selectInvitation(invitation: VisitorInvitation) {
        repository.setSelectedInvitation(invitation)
    }
}
