package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.PatrolPointMutations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatrolPointsViewModel @Inject constructor(
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val mutations: PatrolPointMutations,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatrolPointsUiState(isLoading = true))
    val uiState: StateFlow<PatrolPointsUiState> = _uiState.asStateFlow()

    /**
     * La pantalla lo llama en cada ON_RESUME (entrada y vuelta del
     * formulario), así que no se carga en el `init`: se duplicaría.
     */
    fun onRefresh() {
        val complexId = activeComplexId() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getPatrolPointsUseCase(complexId).fold(
                onSuccess = { points ->
                    _uiState.value = PatrolPointsUiState(points = points)
                },
                onFailure = { error ->
                    _uiState.value = PatrolPointsUiState(
                        errorMessage = error.message ?: PatrolConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onMove(pointId: String, delta: Int) {
        val complexId = activeComplexId() ?: return
        val points = _uiState.value.points
        val from = points.indexOfFirst { it.id == pointId }
        if (from == -1) return
        val to = (from + delta).coerceIn(0, points.lastIndex)
        if (to == from) return
        viewModelScope.launch {
            mutations.move(complexId, pointId, to).fold(
                onSuccess = { onRefresh() },
                onFailure = { error -> showError(error.message) }
            )
        }
    }

    fun onToggleActive(pointId: String, isActive: Boolean) {
        val complexId = activeComplexId() ?: return
        viewModelScope.launch {
            mutations.setActive(complexId, pointId, isActive).fold(
                onSuccess = { onRefresh() },
                onFailure = { error -> showError(error.message) }
            )
        }
    }

    fun onRequestDelete(point: PatrolControlPoint) {
        _uiState.value = _uiState.value.copy(pendingDelete = point)
    }

    fun onDismissDelete() {
        _uiState.value = _uiState.value.copy(pendingDelete = null)
    }

    fun onConfirmDelete() {
        val complexId = activeComplexId() ?: return
        val pointId = _uiState.value.pendingDelete?.id ?: return
        _uiState.value = _uiState.value.copy(pendingDelete = null)
        viewModelScope.launch {
            mutations.delete(complexId, pointId).fold(
                onSuccess = { onRefresh() },
                onFailure = { showError(it.message ?: PatrolConstants.MSG_DELETE_ERROR) }
            )
        }
    }

    private fun showError(message: String?) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message ?: PatrolConstants.MSG_SAVE_ERROR
        )
    }

    private fun activeComplexId(): String? {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = PatrolPointsUiState(
                errorMessage = PatrolConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return null
        }
        return complexId
    }
}
