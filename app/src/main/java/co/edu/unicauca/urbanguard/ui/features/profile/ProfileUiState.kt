package co.edu.unicauca.urbanguard.ui.features.profile

import co.edu.unicauca.urbanguard.domain.model.UserRole

data class ProfileUiState(
    val userName: String = "",
    val userRole: String = "",
    val role: UserRole? = null
)