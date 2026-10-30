package co.edu.unicauca.urbanguard.ui.features.pqrs

import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsSummary

enum class PqrsFilter(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    IN_PROGRESS("En proceso"),
    RESOLVED("Resueltas"),
    REJECTED("Rechazadas");

    fun matches(status: PqrsStatus): Boolean = when (this) {
        ALL -> true
        PENDING -> status == PqrsStatus.SUBMITTED || status == PqrsStatus.IN_REVIEW
        IN_PROGRESS -> status == PqrsStatus.IN_PROGRESS
        RESOLVED -> status == PqrsStatus.RESOLVED
        REJECTED -> status == PqrsStatus.REJECTED
    }
}

data class PqrsInboxUiState(
    val isLoading: Boolean = true,
    val items: List<PqrsSummary> = emptyList(),
    val activeFilter: PqrsFilter = PqrsFilter.ALL,
    val query: String = "",
    val errorMessage: String? = null
) {
    val visibleItems: List<PqrsSummary>
        get() = items.filter { activeFilter.matches(it.status) }.filter { item ->
            query.isBlank() ||
                item.subject.contains(query, ignoreCase = true) ||
                item.authorName.contains(query, ignoreCase = true)
        }
}

