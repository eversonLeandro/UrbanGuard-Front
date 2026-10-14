package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolHistoryFilter
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Lista de turnos del historial de patrullaje (HU-034).
 *
 * Resuelve el conjunto activo y delega el filtrado y los conteos al backend: no
 * calcula cumplimiento ni meta. El rango por defecto (7 días) sale de un [Clock]
 * inyectado para poder fijarlo en los tests. Si el usuario elige un rango
 * inválido no se llama a la API: se muestra el mensaje de validación y se
 * conserva la elección (no se reordena ni se recorta sola).
 */
@HiltViewModel
class PatrolHistoryViewModel @Inject constructor(
    private val getPatrolHistoryUseCase: GetPatrolHistoryUseCase,
    private val sessionManager: SessionManager,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PatrolHistoryUiState(filter = PatrolHistoryFilter.default(clock))
    )
    val uiState: StateFlow<PatrolHistoryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        onRefresh()
    }

    fun onRefresh() = load(_uiState.value.filter)

    fun onFromSelected(date: LocalDate) =
        applyRange(_uiState.value.filter.copy(from = date))

    fun onToSelected(date: LocalDate) =
        applyRange(_uiState.value.filter.copy(to = date))

    fun onOnlyIncompleteChange(value: Boolean) =
        load(_uiState.value.filter.copy(onlyIncomplete = value))

    private fun applyRange(filter: PatrolHistoryFilter) {
        val rangeError = filter.validate()
        if (rangeError != null) {
            loadJob?.cancel()
            _uiState.value = _uiState.value.copy(
                filter = filter,
                isLoading = false,
                errorMessage = null,
                shifts = emptyList(),
                rangeError = rangeError
            )
            return
        }
        load(filter)
    }

    private fun load(filter: PatrolHistoryFilter) {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                filter = filter,
                isLoading = false,
                rangeError = null,
                errorMessage = PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return
        }
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(
            filter = filter,
            isLoading = true,
            errorMessage = null,
            rangeError = null
        )
        loadJob = viewModelScope.launch {
            getPatrolHistoryUseCase(
                complexId = complexId,
                from = filter.from,
                to = filter.to,
                guardId = filter.guardId,
                onlyIncomplete = filter.onlyIncomplete
            ).fold(
                onSuccess = { shifts ->
                    _uiState.value = _uiState.value.copy(isLoading = false, shifts = shifts)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: PatrolHistoryConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }
}
