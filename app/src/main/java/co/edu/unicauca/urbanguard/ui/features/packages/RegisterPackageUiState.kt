package co.edu.unicauca.urbanguard.ui.features.packages

data class RegisterPackageUiState(
    val recipientName: String = "",
    val residentialUnitLabel: String = "",
    val sender: String = "",
    val description: String = "",
    val observations: String = "",
    val photoPath: String? = null,
    val validationError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)
