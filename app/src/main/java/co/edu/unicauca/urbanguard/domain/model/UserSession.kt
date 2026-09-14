package co.edu.unicauca.urbanguard.domain.model

data class UserSession(
    val token: String,
    val userId: String,
    val name: String,
    val role: UserRole,
    val hasConjunto: Boolean = false
)

enum class UserRole { USER, RESIDENT, GUARD, ADMIN }
