package co.edu.unicauca.urbanguard.ui.features.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation

import java.time.LocalDate

/**
 * Estado de la pantalla de visitantes: historial de invitaciones del
 * residente en el conjunto activo, con su estado funcional.
 */
data class VisitorPassListUiState(
    val isLoading: Boolean = false,
    val allPasses: List<VisitorInvitation> = emptyList(),
    val filteredPasses: List<VisitorInvitation> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val dateFilter: LocalDate? = null
) {
    val isEmpty: Boolean = filteredPasses.isEmpty() && !isLoading && errorMessage == null
}
