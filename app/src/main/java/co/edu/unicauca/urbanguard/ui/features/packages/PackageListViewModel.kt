package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import co.edu.unicauca.urbanguard.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PackageListViewModel @Inject constructor(
    private val repository: PackageRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackageListUiState())
    val uiState: StateFlow<PackageListUiState> = _uiState.asStateFlow()

    fun canRegister(): Boolean = sessionManager.currentRole() == UserRole.GUARD

    fun load(status: String? = null) {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = PackageListUiState(errorMessage = "Seleccione un conjunto activo")
            return
        }
        _uiState.value = PackageListUiState(isLoading = true)
        viewModelScope.launch {
            repository.list(complexId, status).fold(
                onSuccess = { _uiState.value = PackageListUiState(packages = it) },
                onFailure = { _uiState.value = PackageListUiState(errorMessage = it.message ?: "No se pudieron cargar los paquetes") }
            )
        }
    }

    fun deliver(packageId: String, deliveredTo: String) {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        viewModelScope.launch {
            repository.deliver(complexId, packageId, deliveredTo).fold(
                onSuccess = { load() },
                onFailure = { error -> _uiState.value = _uiState.value.copy(errorMessage = error.message) }
            )
        }
    }
}
