package co.edu.unicauca.urbanguard.ui.features.admin

import co.edu.unicauca.urbanguard.domain.model.GuardMembership

enum class GuardStatusFilter(val label: String, val apiValue: String) {
    ACTIVE("Activos", "activos"),
    INACTIVE("Inactivos", "inactivos"),
    ALL("Todos", "todos")
}

data class GuardManagementUiState(
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false,
    val guards: List<GuardMembership> = emptyList(),
    val statusFilter: GuardStatusFilter = GuardStatusFilter.ACTIVE,
    val changingMembershipId: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
