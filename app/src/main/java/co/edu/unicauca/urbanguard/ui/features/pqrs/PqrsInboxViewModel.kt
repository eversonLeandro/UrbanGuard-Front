package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PqrsConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetAllPqrsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PqrsInboxViewModel @Inject constructor(
    private val getAllPqrsUseCase: GetAllPqrsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PqrsInboxUiState())
    val uiState: StateFlow<PqrsInboxUiState> = _uiState.asStateFlow()

    fun onRefresh() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = PqrsInboxUiState(
                isLoading = false,
                errorMessage = PqrsConstants.NO_ACTIVE_COMPLEX
            )
            return
        }
        viewModelScope.launch {
            getAllPqrsUseCase(complexId).fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = items,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No fue posible cargar las PQRS."
                    )
                }
            )
        }
    }

    fun onFilterSelected(filter: PqrsFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }
}

