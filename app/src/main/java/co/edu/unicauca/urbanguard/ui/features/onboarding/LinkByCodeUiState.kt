package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest

data class LinkByCodeUiState(
    val code: String = "",
    val codeError: String? = null,

    // Apartamento del futuro residente (solo dígitos, sin negativos)
    val apartamento: String = "",
    val apartamentoError: String? = null,

    // Estado de búsqueda
    val isSearching: Boolean = false,
    val conjuntoFound: ConjuntoSearch? = null,
    val searchError: String? = null,

    // Estado de solicitud
    val isRequesting: Boolean = false,
    val linkRequest: LinkRequest? = null,
    val requestError: String? = null,

    // true cuando la solicitud fue enviada exitosamente
    val requestSuccess: Boolean = false
)