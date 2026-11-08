package co.edu.unicauca.urbanguard.ui.features.guard

import java.time.Instant

data class GuardHomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val conjuntoName: String = "",
    val profilePhotoUrl: String? = null,
    val profilePhotoExpiresAt: Instant? = null
)
