package co.edu.unicauca.urbanguard.ui.features.home

import co.edu.unicauca.urbanguard.domain.model.UserRole

data class HomeUiState(
    val userName: String = "",
    val userRole: String = "",
    val role: UserRole? = null
)
