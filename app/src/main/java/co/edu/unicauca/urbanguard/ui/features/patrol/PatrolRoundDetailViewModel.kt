package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetRoundComplianceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Nombres de los argumentos de la ruta de detalle de ronda (como en NavGraph). */
internal const val ARG_OCCURRENCE_ID = "occurrenceId"
internal const val ARG_GUARD_NAME = "guardName"

/**
 * Detalle de una ronda (nivel 3 del historial de patrullaje).
 *
 * Recibe el `occurrenceId` de la ocurrencia por navegación y pide al backend
 * los puntos NFC esperados vs registrados; el veredicto (estado y cumplimiento)
 * ya viene calculado. Si faltan argumentos no se consulta y se informa.
 */
@HiltViewModel
class PatrolRoundDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRoundComplianceUseCase: GetRoundComplianceUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val occurrenceIdArg: String? =
        savedStateHandle.get<String>(ARG_OCCURRENCE_ID)?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(
        PatrolRoundDetailUiState(
            occurrenceId = occurrenceIdArg,
            guardName = savedStateHandle.get<String>(ARG_GUARD_NAME)?.takeIf { it.isNotBlank() }
        )
    )
    val uiState: StateFlow<PatrolRoundDetailUiState> = _uiState.asStateFlow()

    init {
        onRetry()
    }

    fun onRetry() {
        val occurrenceId = _uiState.value.occurrenceId
        if (occurrenceId == null) {
            failWith(PatrolHistoryConstants.MSG_ROUND_ARGS)
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            failWith(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getRoundComplianceUseCase(complexId, occurrenceId).fold(
                onSuccess = { detail ->
                    _uiState.value = _uiState.value.copy(isLoading = false, detail = detail)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: PatrolHistoryConstants.MSG_ROUND_DETAIL_ERROR
                    )
                }
            )
        }
    }

    private fun failWith(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }
}
