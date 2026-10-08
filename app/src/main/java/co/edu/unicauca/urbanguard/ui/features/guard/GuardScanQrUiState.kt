package co.edu.unicauca.urbanguard.ui.features.guard

/**
 * Estado de la validación de un pase QR (flujo guardia).
 */
data class GuardScanQrUiState(
    val isLoading: Boolean = false,
    val result: co.edu.unicauca.urbanguard.domain.model.VisitorValidationResult? = null,
    val failure: co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure? = null,
    val errorMessage: String? = null
)
