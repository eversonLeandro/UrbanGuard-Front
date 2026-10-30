package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetOwnPqrsDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPqrsDetailUiState(
    val isLoading: Boolean = true,
    val detail: ResidentPqrsDetail? = null,
    val error: AppError? = null
)

@HiltViewModel
class MyPqrsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOwnDetail: GetOwnPqrsDetailUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val pqrsId: String = savedStateHandle["pqrsId"] ?: ""
    private val _uiState = MutableStateFlow(MyPqrsDetailUiState())
    val uiState: StateFlow<MyPqrsDetailUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.session.map {
                Triple(it?.userId, it?.conjuntoActivoId, it?.role)
            }.distinctUntilChanged().collect {
                load()
            }
        }
    }

    fun load() {
        loadJob?.cancel()
        val session = sessionManager.session.value
        val complexId = session?.conjuntoActivoId
        if (session?.role != UserRole.RESIDENT) {
            _uiState.value = MyPqrsDetailUiState(
                isLoading = false,
                error = AppError.Forbidden(AppError.Forbidden.MESSAGE)
            )
            return
        }
        if (complexId.isNullOrBlank()) {
            _uiState.value = MyPqrsDetailUiState(
                isLoading = false,
                error = AppError.Validation("No tienes un conjunto activo seleccionado.")
            )
            return
        }
        if (pqrsId.isBlank()) {
            _uiState.value = MyPqrsDetailUiState(
                isLoading = false,
                error = AppError.NotFound("No encontramos esta PQRS.")
            )
            return
        }
        _uiState.value = MyPqrsDetailUiState()
        loadJob = viewModelScope.launch {
            getOwnDetail(complexId, pqrsId).fold(
                onSuccess = { _uiState.value = MyPqrsDetailUiState(isLoading = false, detail = it) },
                onFailure = { failure ->
                    val error = AppError.from(failure)
                    _uiState.value = MyPqrsDetailUiState(
                        isLoading = false,
                        error = if (error is AppError.NotFound) {
                            AppError.NotFound("No encontramos esta PQRS.")
                        } else error
                    )
                }
            )
        }
    }
}
