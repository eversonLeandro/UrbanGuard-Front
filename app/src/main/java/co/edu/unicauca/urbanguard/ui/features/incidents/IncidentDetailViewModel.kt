package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Entrega la novedad elegida en la bandeja.
 *
 * Viaja por memoria del repositorio (igual que el pase y la invitación),
 * así la pantalla no necesita argumentos de navegación.
 */
@HiltViewModel
class IncidentDetailViewModel @Inject constructor(
    repository: IncidentRepository
) : ViewModel() {

    private val _incident = MutableStateFlow<SecurityIncident?>(
        repository.selectedIncident()
    )
    val incident: StateFlow<SecurityIncident?> = _incident.asStateFlow()
}
