package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PqrsConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetPqrsDetailUseCase
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.RespondPqrsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PqrsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDetailUseCase: GetPqrsDetailUseCase,
    private val respondUseCase: RespondPqrsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val pqrsId: String = savedStateHandle.get<String>("pqrsId").orEmpty()
    private val _uiState = MutableStateFlow(PqrsDetailUiState(isLoading = true))
    val uiState: StateFlow<PqrsDetailUiState> = _uiState.asStateFlow()

    fun load() {
        val complexId = activeComplexId() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getDetailUseCase(complexId, pqrsId).fold(
                onSuccess = { detail -> _uiState.value = PqrsDetailUiState(detail = detail, isLoading = false) },
                onFailure = { error -> _uiState.value = PqrsDetailUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "No fue posible cargar la PQRS."
                ) }
            )
        }
    }

    fun openDialog() {
        _uiState.value = _uiState.value.copy(isDialogOpen = true, submitErrorMessage = null)
    }

    fun dismissDialog() {
        if (!_uiState.value.isSubmitting) {
            _uiState.value = _uiState.value.copy(isDialogOpen = false, response = "", submitErrorMessage = null)
        }
    }

    fun onTargetStatusChange(status: PqrsStatus) {
        _uiState.value = _uiState.value.copy(targetStatus = status)
    }

    fun onResponseChange(response: String) {
        _uiState.value = _uiState.value.copy(response = response.take(PqrsConstants.RESPONSE_MAX_LENGTH + 1))
    }

    fun submit() {
        val state = _uiState.value
        val detail = state.detail ?: return
        val complexId = activeComplexId() ?: return
        val error = when {
            state.response.trim().isBlank() -> PqrsConstants.RESPONSE_REQUIRED
            state.response.trim().length > PqrsConstants.RESPONSE_MAX_LENGTH -> PqrsConstants.RESPONSE_TOO_LONG
            else -> null
        }
        if (error != null) {
            _uiState.value = state.copy(submitErrorMessage = error)
            return
        }
        _uiState.value = state.copy(isSubmitting = true, submitErrorMessage = null)
        viewModelScope.launch {
            respondUseCase(complexId, detail.id, state.targetStatus, state.response).fold(
                onSuccess = { updated -> _uiState.value = _uiState.value.copy(
                    detail = updated,
                    isSubmitting = false,
                    isDialogOpen = false,
                    response = "",
                    successMessage = "PQRS actualizada correctamente."
                ) },
                onFailure = { failure -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage = failure.message ?: "No fue posible actualizar la PQRS."
                ) }
            )
        }
    }

    fun consumeSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun activeComplexId(): String? {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = PqrsDetailUiState(
                isLoading = false,
                errorMessage = PqrsConstants.NO_ACTIVE_COMPLEX
            )
        }
        return complexId
    }
}

data class PqrsDetailUiState(
    val isLoading: Boolean = false,
    val detail: PqrsDetail? = null,
    val errorMessage: String? = null,
    val isDialogOpen: Boolean = false,
    val targetStatus: PqrsStatus = PqrsStatus.IN_PROGRESS,
    val response: String = "",
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null,
    val successMessage: String? = null
) {
    val canManage: Boolean
        get() = detail?.status?.isTerminal == false
}

