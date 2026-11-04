package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.usecase.residential.GetCensusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CensusViewModel @Inject constructor(
    private val getCensusUseCase: GetCensusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CensusUiState())
    val uiState: StateFlow<CensusUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var hasObservedScreenResume = false

    init {
        onRefresh()
    }

    fun onRefresh() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            loadJob?.cancel()
            _uiState.value = CensusUiState(
                error = AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE)
            )
            return
        }

        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadJob = viewModelScope.launch {
            getCensusUseCase(complexId).fold(
                onSuccess = { units ->
                    val selectedId = _uiState.value.selectedUnitId
                        ?.takeIf { selectedId -> units.any { it.id == selectedId } }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        units = units,
                        error = null,
                        selectedUnitId = selectedId
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = AppError.from(error)
                    )
                }
            )
        }
    }

    fun onScreenResumed() {
        if (hasObservedScreenResume) {
            onRefresh()
        } else {
            hasObservedScreenResume = true
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterChange(filter: CensusUnitFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun onUnitSelected(unitId: String) {
        if (_uiState.value.units.any { it.id == unitId }) {
            _uiState.value = _uiState.value.copy(selectedUnitId = unitId)
        }
    }

    fun onDismissUnitDetail() {
        _uiState.value = _uiState.value.copy(selectedUnitId = null)
    }

    private companion object {
        const val NO_ACTIVE_COMPLEX_MESSAGE = "No hay un conjunto activo seleccionado."
    }
}
