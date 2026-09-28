package co.edu.unicauca.urbanguard.ui.features.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization

/**
 * Estado de la pantalla de visitantes, que muestra el historial de pases QR
 * del conjunto activo.
 *
 * Mientras el endpoint `GET api/visitors/authorizations` no exista en el
 * backend, [passes] llega vacío y la pantalla muestra su estado vacío.
 */
data class VisitorPassListUiState(
    val isLoading: Boolean = false,
    val passes: List<VisitorAuthorization> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = passes.isEmpty() && !isLoading && errorMessage == null
}
