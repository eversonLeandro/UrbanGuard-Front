package co.edu.unicauca.urbanguard.ui.features.pqrs

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary

data class MyPqrsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val items: List<ResidentPqrsSummary> = emptyList(),
    val statusFilter: PqrsStatus? = null,
    val typeFilter: PqrsType? = null,
    val hasAnyPqrs: Boolean? = null,
    val error: AppError? = null
) {
    val hasFilters: Boolean get() = statusFilter != null || typeFilter != null
    val isGeneralEmpty: Boolean get() = !isLoading && error == null && items.isEmpty() && hasAnyPqrs == false
    val isFilteredEmpty: Boolean
        get() = !isLoading && error == null && items.isEmpty() && hasFilters && hasAnyPqrs == true
}

