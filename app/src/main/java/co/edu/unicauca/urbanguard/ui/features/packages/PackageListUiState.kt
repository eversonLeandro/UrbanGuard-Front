package co.edu.unicauca.urbanguard.ui.features.packages

import co.edu.unicauca.urbanguard.domain.model.Package

data class PackageListUiState(
    val isLoading: Boolean = false,
    val packages: List<Package> = emptyList(),
    val errorMessage: String? = null
)
