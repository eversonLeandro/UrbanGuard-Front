package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConjuntoSelectionViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConjuntoSelectionUiState())
    val uiState: StateFlow<ConjuntoSelectionUiState> = _uiState.asStateFlow()

    init {
        loadConjuntos()
    }

    fun loadConjuntos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // TODO: reemplazar por llamada real al backend que retorna la
                // lista de conjuntos asocidos del usuario (GET).
                val session = sessionManager.session.value
                _uiState.value = ConjuntoSelectionUiState(
                    isLoading = false,
                    conjuntos = session?.conjuntosAsociados ?: emptyList(),
                    conjuntoActivoId = session?.conjuntoActivoId,
                    userName = session?.name.orEmpty()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ocurrió un error al cargar tus conjuntos."
                )
            }
        }
    }

    fun onConjuntoSelected(conjuntoId: String) {
        sessionManager.setActiveConjunto(conjuntoId)
        _uiState.value = _uiState.value.copy(conjuntoActivoId = conjuntoId)
    }
}