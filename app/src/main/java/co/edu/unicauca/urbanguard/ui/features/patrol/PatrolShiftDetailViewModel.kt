package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.usecase.patrol.CheckShiftComplianceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Nombres de los argumentos de la ruta de detalle (deben coincidir con NavGraph). */
internal const val ARG_SHIFT_TYPE = "shiftType"
internal const val ARG_DATE = "date"

/**
 * Detalle y cumplimiento de un turno (HU-032).
 *
 * Recibe tipo y fecha por navegación y pide al backend el detalle, que ya trae
 * `goalMet`: el frontend no calcula la meta. Si faltan o son inválidos los
 * argumentos no se consulta y se informa del problema.
 */
@HiltViewModel
class PatrolShiftDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkShiftComplianceUseCase: CheckShiftComplianceUseCase,
    private val sessionManager: SessionManager,
    clock: Clock
) : ViewModel() {

    private val shiftTypeArg: String? = savedStateHandle.get<String>(ARG_SHIFT_TYPE)
    private val dateArg: String? = savedStateHandle.get<String>(ARG_DATE)

    private val parsedShiftType: PatrolShiftType? = PatrolShiftType.fromWire(shiftTypeArg)
    private val parsedDate: LocalDate? =
        dateArg?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    private val shiftType: PatrolShiftType = parsedShiftType ?: PatrolShiftType.DAY
    private val date: LocalDate = parsedDate ?: LocalDate.now(clock)

    private val _uiState = MutableStateFlow(
        PatrolShiftDetailUiState(shiftType = shiftType, date = date)
    )
    val uiState: StateFlow<PatrolShiftDetailUiState> = _uiState.asStateFlow()

    init {
        onRetry()
    }

    fun onRetry() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            failWith(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        if (parsedShiftType == null || parsedDate == null) {
            failWith(PatrolHistoryConstants.MSG_DETAIL_ARGS)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            checkShiftComplianceUseCase(complexId, parsedShiftType, parsedDate).fold(
                onSuccess = { detail ->
                    _uiState.value = _uiState.value.copy(isLoading = false, detail = detail)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: PatrolHistoryConstants.MSG_DETAIL_ERROR
                    )
                }
            )
        }
    }

    private fun failWith(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }
}
