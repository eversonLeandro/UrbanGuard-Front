package co.edu.unicauca.urbanguard.domain.model

data class UserSession(
    val token: String,
    val userId: String,
    val name: String,
    val role: UserRole
)

enum class UserRole { RESIDENT, GUARD, ADMIN }
