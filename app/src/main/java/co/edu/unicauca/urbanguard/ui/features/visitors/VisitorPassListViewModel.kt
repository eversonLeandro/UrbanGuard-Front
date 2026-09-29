package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.domain.usecase.visitors.GetVisitorAuthorizationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitorPassListViewModel @Inject constructor(
    private val getVisitorAuthorizationsUseCase: GetVisitorAuthorizationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitorPassListUiState())
    val uiState: StateFlow<VisitorPassListUiState> = _uiState.asStateFlow()

    init {
        onRefresh()
    }

    /**
     * Vuelve a pedir el historial. Se llama al entrar a la pantalla para que
     * el pase generado en el formulario aparezca al volver al listado.
     */
    fun onRefresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getVisitorAuthorizationsUseCase().fold(
                onSuccess = { passes ->
                    _uiState.value = VisitorPassListUiState(passes = passes)
                },
                onFailure = { error ->
                    _uiState.value = VisitorPassListUiState(
                        errorMessage = error.message ?: VisitorConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }
}
