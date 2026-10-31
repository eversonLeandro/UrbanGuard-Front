package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolRouteValidation
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import co.edu.unicauca.urbanguard.domain.model.validatePatrolRoute
import co.edu.unicauca.urbanguard.domain.model.weekDays
import co.edu.unicauca.urbanguard.domain.model.withAddedSchedule
import co.edu.unicauca.urbanguard.domain.model.withPickedTime
import co.edu.unicauca.urbanguard.domain.model.withToggledDay
import co.edu.unicauca.urbanguard.domain.model.withoutSchedule
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolRoutesUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.SavePatrolRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

/** Eventos de una sola entrega del formulario de ruta. */
sealed interface PatrolRouteFormEvent {
    /** La ruta quedó guardada; la pantalla debe volver al listado. */
    data object Saved : PatrolRouteFormEvent
}

/** Acciones sobre los horarios de la ruta. */
sealed interface RouteScheduleEvent {
    /** Agrega una repetición vacía. */
    data object Add : RouteScheduleEvent

    /** Quita la repetición indicada. */
    data class Remove(val index: Int) : RouteScheduleEvent

    /** Abre el reloj para la repetición indicada. */
    data class OpenPicker(val index: Int) : RouteScheduleEvent

    /** Cierra el reloj sin cambios. */
    data object ClosePicker : RouteScheduleEvent

    /** Fija la hora elegida en el reloj abierto. */
    data class PickTime(val hour: Int, val minute: Int) : RouteScheduleEvent

    /** Marca o desmarca un día de la repetición indicada. */
    data class ToggleDay(
        val index: Int,
        val day: DayOfWeek,
        val selected: Boolean
    ) : RouteScheduleEvent
}

@HiltViewModel
class PatrolRouteFormViewModel @Inject constructor(
    private val getPatrolRoutesUseCase: GetPatrolRoutesUseCase,
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val savePatrolRouteUseCase: SavePatrolRouteUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatrolRouteFormUiState())
    val uiState: StateFlow<PatrolRouteFormUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<PatrolRouteFormEvent>(Channel.BUFFERED)
    val events: Flow<PatrolRouteFormEvent> = eventChannel.receiveAsFlow()

    private var routeId: String = ""

    fun load(routeId: String?) {
        this.routeId = routeId.orEmpty()
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        viewModelScope.launch {
            getPatrolPointsUseCase(complexId).fold(
                onSuccess = { points ->
                    val options = points.map {
                        PatrolPointOption(id = it.id, name = it.name)
                    }
                    _uiState.value = _uiState.value.copy(availablePointIds = options)
                    loadRoute(complexId, routeId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: PatrolConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null, errorMessage = null)
    }

    fun onTogglePoint(pointId: String, selected: Boolean) {
        val current = _uiState.value.selectedIds.toMutableList()
        if (selected) {
            if (!current.contains(pointId)) current.add(pointId)
        } else {
            current.remove(pointId)
        }
        _uiState.value = _uiState.value.copy(selectedIds = current, formError = null)
    }

    /** Mueve una parada dentro de [PatrolRouteFormUiState.selectedIds]. */
    fun onMovePoint(pointId: String, delta: Int) {
        val current = _uiState.value.selectedIds.toMutableList()
        val from = current.indexOf(pointId)
        val to = from + delta
        if (from == -1 || to !in current.indices) return
        val moved = current.removeAt(from)
        current.add(to, moved)
        _uiState.value = _uiState.value.copy(selectedIds = current, formError = null)
    }

    fun onScheduleEvent(event: RouteScheduleEvent) {
        val schedules = _uiState.value.schedules
        when (event) {
            RouteScheduleEvent.Add ->
                _uiState.value = _uiState.value.copy(
                    schedules = schedules.withAddedSchedule(),
                    formError = null
                )
            is RouteScheduleEvent.Remove ->
                _uiState.value = _uiState.value.copy(
                    schedules = schedules.withoutSchedule(event.index)
                )
            is RouteScheduleEvent.OpenPicker ->
                _uiState.value = _uiState.value.copy(editingTimeIndex = event.index)
            RouteScheduleEvent.ClosePicker ->
                _uiState.value = _uiState.value.copy(editingTimeIndex = null)
            is RouteScheduleEvent.PickTime ->
                _uiState.value = _uiState.value.copy(
                    schedules = schedules.withPickedTime(
                        _uiState.value.editingTimeIndex ?: return,
                        event.hour,
                        event.minute
                    ),
                    editingTimeIndex = null
                )
            is RouteScheduleEvent.ToggleDay ->
                _uiState.value = _uiState.value.copy(
                    schedules = schedules.withToggledDay(
                        event.index,
                        event.day,
                        event.selected
                    ),
                    formError = null
                )
        }
    }

    fun onActiveChange(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = isActive)
    }

    /** Agrega un horario preset (p. ej. mañana 08:00 o noche 22:00). */
    fun onAddPreset(hour: Int, minute: Int, everyDay: Boolean) {
        val days = if (everyDay) {
            weekDays().toSet()
        } else {
            defaultWorkDays()
        }
        val current = _uiState.value.schedules.toMutableList()
        current.add(PatrolSchedule(time = LocalTime.of(hour, minute), days = days))
        _uiState.value = _uiState.value.copy(schedules = current, formError = null)
    }

    fun onSaveClick() {
        val state = _uiState.value
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = PatrolConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        when (validatePatrolRoute(state.name, state.selectedIds, state.schedules)) {
            PatrolRouteValidation.NAME_REQUIRED ->
                _uiState.value = state.copy(nameError = "Ponle un nombre a la ruta.")
            PatrolRouteValidation.NAME_TOO_LONG ->
                _uiState.value = state.copy(nameError = PatrolConstants.MSG_NAME_TOO_LONG)
            PatrolRouteValidation.NO_POINTS ->
                _uiState.value = state.copy(formError = "Elige al menos un punto de la ruta.")
            PatrolRouteValidation.NO_SCHEDULES ->
                _uiState.value = state.copy(formError = "Agrega al menos un horario.")
            PatrolRouteValidation.SCHEDULE_WITHOUT_DAYS ->
                _uiState.value = state.copy(
                    formError = "Cada horario necesita al menos un día."
                )
            PatrolRouteValidation.VALID -> saveRoute(state, complexId)
        }
    }

    private fun saveRoute(state: PatrolRouteFormUiState, complexId: String) {
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            savePatrolRouteUseCase(
                PatrolRoute(
                    id = routeId,
                    complexId = complexId,
                    name = state.name.trim(),
                    pointIds = state.selectedIds,
                    schedules = state.schedules,
                    isActive = state.isActive
                )
            ).fold(
                onSuccess = { eventChannel.send(PatrolRouteFormEvent.Saved) },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: PatrolConstants.MSG_SAVE_ERROR
                    )
                }
            )
        }
    }

    private suspend fun loadRoute(complexId: String, routeId: String?) {
        if (routeId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                schedules = listOf(defaultPatrolSchedule())
            )
            return
        }
        getPatrolRoutesUseCase(complexId).fold(
            onSuccess = { routes ->
                routes.firstOrNull { it.id == routeId }?.let { route ->
                    _uiState.value = _uiState.value.copy(
                        name = route.name,
                        selectedIds = route.pointIds,
                        schedules = route.schedules,
                        isActive = route.isActive
                    )
                }
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: PatrolConstants.MSG_LIST_ERROR
                )
            }
        )
    }
}
