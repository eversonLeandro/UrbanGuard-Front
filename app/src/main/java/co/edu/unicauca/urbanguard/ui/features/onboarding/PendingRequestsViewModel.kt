package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.model.LinkRequestState
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetLinkRequestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingRequestsViewModel @Inject constructor(
    private val getLinkRequestsUseCase: GetLinkRequestsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingRequestsUiState())
    val uiState: StateFlow<PendingRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    private fun loadRequests() {
        if (_uiState.value.isLoading) _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getLinkRequestsUseCase().fold(
                onSuccess = { requests ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        requests = requests,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun onFilterSelected(filter: LinkRequestState) {
        if (_uiState.value.activeFilter == filter) return
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun onErrorDismissed() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}