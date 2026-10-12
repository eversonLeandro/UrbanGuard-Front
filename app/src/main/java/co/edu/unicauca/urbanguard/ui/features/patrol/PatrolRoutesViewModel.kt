package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.usecase.patrol.DeletePatrolRouteUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolRoutesUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.SavePatrolRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatrolRoutesViewModel @Inject constructor(
    private val getPatrolRoutesUseCase: GetPatrolRoutesUseCase,
    private val savePatrolRouteUseCase: SavePatrolRouteUseCase,
    private val deletePatrolRouteUseCase: DeletePatrolRouteUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatrolRoutesUiState(isLoading = true))
    val uiState: StateFlow<PatrolRoutesUiState> = _uiState.asStateFlow()

    /**
     * La pantalla lo llama en cada ON_RESUME (entrada y vuelta del
     * formulario), así que no se carga en el `init`: se duplicaría.
     */
    fun onRefresh() {
        val complexId = activeComplexId() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getPatrolRoutesUseCase(complexId).fold(
                onSuccess = { routes ->
                    _uiState.value = PatrolRoutesUiState(routes = routes)
                },
                onFailure = { error ->
                    _uiState.value = PatrolRoutesUiState(
                        errorMessage = error.message ?: PatrolConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onToggleActive(route: PatrolRoute, isActive: Boolean) {
        viewModelScope.launch {
            savePatrolRouteUseCase(route.copy(isActive = isActive)).fold(
                onSuccess = { onRefresh() },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: PatrolConstants.MSG_SAVE_ERROR
                    )
                }
            )
        }
    }

    fun onRequestDelete(route: PatrolRoute) {
        _uiState.value = _uiState.value.copy(pendingDelete = route)
    }

    fun onDismissDelete() {
        _uiState.value = _uiState.value.copy(pendingDelete = null)
    }

    fun onConfirmDelete() {
        val complexId = activeComplexId() ?: return
        val routeId = _uiState.value.pendingDelete?.id ?: return
        _uiState.value = _uiState.value.copy(pendingDelete = null)
        viewModelScope.launch {
            deletePatrolRouteUseCase(complexId, routeId).fold(
                onSuccess = { onRefresh() },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: PatrolConstants.MSG_DELETE_ERROR
                    )
                }
            )
        }
    }

    private fun activeComplexId(): String? {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = PatrolRoutesUiState(
                errorMessage = PatrolConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return null
        }
        return complexId
    }
}
