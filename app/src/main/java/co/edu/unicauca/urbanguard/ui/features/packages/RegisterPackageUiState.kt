package co.edu.unicauca.urbanguard.ui.features.packages

data class RegisterPackageUiState(
    val residentSearch: String = "",
    val selectedResident: PackageRecipientOption? = null,
    val residents: List<PackageRecipientOption> = emptyList(),
    val isLoadingResidents: Boolean = false,
    val sender: String = "",
    val description: String = "",
    val photoPath: String? = null,
    val validationError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

data class PackageRecipientOption(
    val key: String,
    val residentId: String?,
    val residentName: String,
    val unitId: String,
    val unitLabel: String
) {
    val displayLabel: String
        get() = "$residentName · Unidad $unitLabel"
}
