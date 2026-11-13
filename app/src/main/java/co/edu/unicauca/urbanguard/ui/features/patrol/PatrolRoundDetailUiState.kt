package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCompliance

/**
 * Estado del detalle de una ronda (nivel 3): puntos NFC esperados vs
 * registrados. [guardName] llega por navegación (el detalle de la ronda del
 * backend solo trae el id del vigilante) y puede estar vacío.
 */
data class PatrolRoundDetailUiState(
    val occurrenceId: String?,
    val guardName: String? = null,
    val isLoading: Boolean = true,
    val detail: PatrolRoundCompliance? = null,
    val errorMessage: String? = null
) {
    /** Ronda cargada y con puntos que listar (por contrato, siempre hay snapshot). */
    val hasCheckpoints: Boolean
        get() = detail?.checkpoints?.isNotEmpty() == true
}
