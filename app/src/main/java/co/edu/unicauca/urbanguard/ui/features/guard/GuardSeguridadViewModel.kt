package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.availableFrom
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolEntryUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.StartScheduledRoundUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/** Eventos de una sola entrega de la entrada de patrullaje. */
sealed interface GuardSeguridadEvent {
    /** Ronda abierta (nueva o en curso); ir a la pantalla activa. */
    data object GoToActiveRound : GuardSeguridadEvent
}

@HiltViewModel
class GuardSeguridadViewModel @Inject constructor(
    private val getPatrolEntryUseCase: GetPatrolEntryUseCase,
    private val startScheduledRoundUseCase: StartScheduledRoundUseCase,
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
        val existing = _uiState.value.activeRound?.takeIf { !it.isFinished() }

        // Si ya hay una ronda activa, redirigir a ella (sin importar la ruta).
        // Regla: solo puede haber UNA ronda en curso simultáneamente.
        if (existing != null) {
            // Si es la misma ocurrencia (o misma ruta con dueAt), continuar.
            if (existing.routeId == route.id && existing.dueAt != null) {
                viewModelScope.launch {
                    eventChannel.send(GuardSeguridadEvent.GoToActiveRound)
                }
            } else {
                // Otra ronda ya está en curso → mostrar aviso.
                val activeName = existing.routeName
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Ya hay una ronda en progreso: \"$activeName\". " +
                        "Termínala antes de iniciar otra."
                )
            }
            return
        }
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        val availableFrom = route.availableFrom(
            now.toLocalDate(),
            PatrolConstants.ROUND_EARLY_START_MINUTES,
            now.toLocalTime()
        )
        if (availableFrom != null && now.toLocalTime().isBefore(availableFrom)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Disponible desde las " +
                    availableFrom.format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                    )
            )
            return
        }
        if (starting) return
        starting = true
        viewModelScope.launch {
            runCatching { openRound(complexId, route) }
                .onSuccess { eventChannel.send(GuardSeguridadEvent.GoToActiveRound) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "No se pudo iniciar la ronda."
                    )
                }
            starting = false
        }
    }

    /**
     * Abre la ronda cubriendo la cita correspondiente (o libre si aún no
     * abre ninguna) y con vencimiento en la siguiente cita programada.
     */
    private suspend fun openRound(complexId: String, route: PatrolRoute) {
        startScheduledRoundUseCase(complexId, route)
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
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
