package co.edu.unicauca.urbanguard.ui.features.home

import co.edu.unicauca.urbanguard.domain.model.UserRole
import java.time.Instant

data class HomeUiState(
    val userName: String = "",
    val userRole: String = "",
    val role: UserRole? = null,
    val conjuntoName: String = "",
    val profilePhotoUrl: String? = null,
    val profilePhotoExpiresAt: Instant? = null
)
