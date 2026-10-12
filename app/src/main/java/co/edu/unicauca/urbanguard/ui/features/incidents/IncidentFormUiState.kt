package co.edu.unicauca.urbanguard.ui.features.incidents

import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint

/**
 * Estado del formulario de novedad.
 *
 * [availablePoints] alimenta el selector opcional del punto donde pasó;
 * [preselectedPointId] llega de la ronda activa para no preguntar dónde.
 */
data class IncidentFormUiState(
    val category: IncidentCategory? = null,
    val description: String = "",
    val photoPath: String? = null,
    val availablePoints: List<PatrolControlPoint> = emptyList(),
    val pointId: String? = null,
    val categoryError: String? = null,
    val descriptionError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val cameraError: Boolean = false
)
