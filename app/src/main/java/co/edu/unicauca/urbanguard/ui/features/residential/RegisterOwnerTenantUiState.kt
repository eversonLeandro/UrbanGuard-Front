package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.ResidentType

enum class OwnerTenantField {
    NOMBRES,
    APELLIDOS,
    DOCUMENTO,
    EMAIL,
    TELEFONO
}

data class RegisterOwnerTenantUiState(
    val unitId: String? = null,
    val type: ResidentType = ResidentType.OWNER,
    val nombres: String = "",
    val apellidos: String = "",
    val documento: String = "",
    val email: String = "",
    val telefono: String = "",
    val isSaving: Boolean = false,
    val fieldErrors: Map<OwnerTenantField, String> = emptyMap(),
    val serverError: AppError? = null,
    val successMessage: String? = null
)
