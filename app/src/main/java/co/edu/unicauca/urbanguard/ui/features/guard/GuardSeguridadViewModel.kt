package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolEntryUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.StartRoundUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Eventos de una sola entrega de la entrada de patrullaje. */
sealed interface GuardSeguridadEvent {
    /** Ronda abierta (nueva o en curso); ir a la pantalla activa. */
    data object GoToActiveRound : GuardSeguridadEvent
}

@HiltViewModel
class GuardSeguridadViewModel @Inject constructor(
    private val getPatrolEntryUseCase: GetPatrolEntryUseCase,
    private val startRoundUseCase: StartRoundUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardSeguridadUiState(isLoading = true))
    val uiState: StateFlow<GuardSeguridadUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<GuardSeguridadEvent>(Channel.BUFFERED)
    val events: Flow<GuardSeguridadEvent> = eventChannel.receiveAsFlow()

    private var starting = false

    fun onRefresh() {
        val complexId = activeComplexId() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { getPatrolEntryUseCase(complexId) }.fold(
                onSuccess = { entry ->
                    _uiState.value = GuardSeguridadUiState(
                        todayRoutes = entry.todayRoutes,
                        activeRound = entry.activeRound,
                        recentRounds = entry.recentRounds
                    )
                },
                onFailure = { error ->
                    _uiState.value = GuardSeguridadUiState(
                        errorMessage = error.message ?: "No se pudo cargar el patrullaje."
                    )
                }
            )
        }
    }

    fun onStartRoute(route: PatrolRoute) {
        val complexId = activeComplexId() ?: return
        // Si ya hay una ronda abierta de esta ruta, se continúa en vez de
        // abrir otra: evita duplicados por doble toque.
        _uiState.value.activeRound
            ?.takeIf { !it.isFinished() && it.routeId == route.id }
            ?.let {
                viewModelScope.launch {
                    eventChannel.send(GuardSeguridadEvent.GoToActiveRound)
                }
                return
            }
        if (starting) return
        starting = true
        viewModelScope.launch {
            runCatching {
                startRoundUseCase(complexId, route.id, route.name, route.pointIds.size)
            }.onSuccess {
                eventChannel.send(GuardSeguridadEvent.GoToActiveRound)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "No se pudo iniciar la ronda."
                )
            }
            starting = false
        }
    }

    fun onContinueRound() {
        viewModelScope.launch {
            eventChannel.send(GuardSeguridadEvent.GoToActiveRound)
        }
    }

    private fun activeComplexId(): String? {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = GuardSeguridadUiState(
                errorMessage = "No tienes un conjunto activo seleccionado."
            )
            return null
        }
        return complexId
    }
}
