package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PackageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PackageRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val packageId: String = checkNotNull(savedStateHandle["packageId"])
    private val _state = MutableStateFlow(PackageDetailUiState())
    val state: StateFlow<PackageDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _state.value = PackageDetailUiState(errorMessage = "No hay un conjunto activo")
            return
        }
        _state.value = PackageDetailUiState(isLoading = true)
        viewModelScope.launch {
            repository.detail(complexId, packageId).fold(
                onSuccess = { _state.value = PackageDetailUiState(parcel = it) },
                onFailure = {
                    _state.value = PackageDetailUiState(
                        errorMessage = it.message ?: "No fue posible cargar el paquete"
                    )
                }
            )
        }
    }
}

data class PackageDetailUiState(
    val isLoading: Boolean = false,
    val parcel: Package? = null,
    val errorMessage: String? = null
)
