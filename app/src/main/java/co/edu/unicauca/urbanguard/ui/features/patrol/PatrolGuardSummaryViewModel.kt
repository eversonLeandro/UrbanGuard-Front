package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolHistoryFilter
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolGuardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Nombres de los argumentos de la ruta de resumen (deben coincidir con NavGraph). */
internal const val ARG_GUARD_ID = "guardId"
internal const val ARG_FROM = "from"
internal const val ARG_TO = "to"

/**
 * Resumen de desempeño de un vigilante (parte del flujo de HU-034).
 *
 * Recibe vigilante y rango por navegación y pide los agregados al backend. Si
 * falta el vigilante o el conjunto activo no se consulta: se informa el
 * problema de argumentos.
 */
@HiltViewModel
class PatrolGuardSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPatrolGuardSummaryUseCase: GetPatrolGuardSummaryUseCase,
    private val sessionManager: SessionManager,
    clock: Clock
) : ViewModel() {

    private val guardId: String = savedStateHandle.get<String>(ARG_GUARD_ID).orEmpty()
    private val from: LocalDate =
        parseArg(savedStateHandle.get<String>(ARG_FROM)) ?: PatrolHistoryFilter.default(clock).from
    private val to: LocalDate =
        parseArg(savedStateHandle.get<String>(ARG_TO)) ?: PatrolHistoryFilter.default(clock).to

    private val _uiState = MutableStateFlow(PatrolGuardSummaryUiState())
    val uiState: StateFlow<PatrolGuardSummaryUiState> = _uiState.asStateFlow()

    init {
        onRetry()
    }

    fun onRetry() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            failWith(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        if (guardId.isBlank()) {
            failWith(PatrolHistoryConstants.MSG_GUARD_SUMMARY_ARGS)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getPatrolGuardSummaryUseCase(complexId, guardId, from, to).fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(isLoading = false, summary = summary)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: PatrolHistoryConstants.MSG_GUARD_SUMMARY_ERROR
                    )
                }
            )
        }
    }

    private fun failWith(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }
}

private fun parseArg(value: String?): LocalDate? =
    value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
