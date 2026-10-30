package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetOwnPqrsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPqrsViewModel @Inject constructor(
    private val getOwnPqrs: GetOwnPqrsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPqrsUiState())
    val uiState: StateFlow<MyPqrsUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.session.map {
                Triple(it?.userId, it?.conjuntoActivoId, it?.role)
            }.distinctUntilChanged().collect {
                loadJob?.cancel()
                _uiState.value = MyPqrsUiState()
                onRefresh()
            }
        }
    }

    fun onStatusSelected(status: PqrsStatus?) {
        if (_uiState.value.statusFilter == status) return
        _uiState.value = _uiState.value.copy(
            statusFilter = status, items = emptyList(), hasAnyPqrs = null
        )
        onRefresh()
    }

    fun onTypeSelected(type: PqrsType?) {
        if (_uiState.value.typeFilter == type) return
        _uiState.value = _uiState.value.copy(
            typeFilter = type, items = emptyList(), hasAnyPqrs = null
        )
        onRefresh()
    }

    fun clearFilters() {
        if (!_uiState.value.hasFilters) return
        _uiState.value = _uiState.value.copy(
            statusFilter = null, typeFilter = null, items = emptyList(), hasAnyPqrs = null
        )
        onRefresh()
    }

    fun onRefresh() {
        loadJob?.cancel()
        val session = sessionManager.session.value
        val complexId = session?.conjuntoActivoId

        if (session?.role != UserRole.RESIDENT) {
            _uiState.value = MyPqrsUiState(
                isLoading = false,
                error = AppError.Forbidden(AppError.Forbidden.MESSAGE)
            )
            return
        }
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                items = emptyList(),
                hasAnyPqrs = null,
                error = AppError.Validation("No tienes un conjunto activo seleccionado.")
            )
            return
        }
        val current = _uiState.value
        _uiState.value = current.copy(
            isLoading = current.items.isEmpty(),
            isRefreshing = current.items.isNotEmpty(),
            error = null
        )
        loadJob = viewModelScope.launch {
            val status = _uiState.value.statusFilter
            val type = _uiState.value.typeFilter
            getOwnPqrs(complexId, status, type).fold(
                onSuccess = { items ->
                    val unfiltered = if (status != null || type != null) {
                        if (items.isEmpty()) getOwnPqrs(complexId) else null
                    } else null
                    val hasAny = when {
                        status == null && type == null -> items.isNotEmpty()
                        items.isNotEmpty() -> true
                        else -> unfiltered?.getOrNull()?.isNotEmpty()
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = items,
                        hasAnyPqrs = hasAny,
                        error = unfiltered?.exceptionOrNull()?.let(AppError::from)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = AppError.from(error)
                    )
                }
            )
        }
    }
}

