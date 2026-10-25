package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentDetailViewModel @Inject constructor(
    private val repository: IncidentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _incident = MutableStateFlow<SecurityIncident?>(null)
    val incident: StateFlow<SecurityIncident?> = _incident.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun load(reportId: String) {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _isLoading.value = false
            _errorMessage.value = "No tienes un conjunto activo seleccionado."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            repository.getIncident(complexId, reportId).fold(
                onSuccess = {
                    _incident.value = it
                    _isLoading.value = false
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "No se pudo cargar la novedad."
                    _isLoading.value = false
                }
            )
        }
    }
}
