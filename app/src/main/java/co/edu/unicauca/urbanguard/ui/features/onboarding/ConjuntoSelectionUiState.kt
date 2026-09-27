package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation

data class ConjuntoSelectionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val conjuntos: List<ConjuntoUserAssociation> = emptyList(),
    val conjuntoActivoId: String? = null,
    val userName: String = ""
)
