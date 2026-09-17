package co.edu.unicauca.urbanguard.domain.model

data class UserSession(
    val token: String,
    val userId: String,
    val name: String,
    val role: UserRole,
    val conjuntosAsociados: List<ConjuntoUserAssociation> = emptyList(),
    val conjuntoActivoId: String? = null
)

enum class UserRole { USER, RESIDENT, GUARD, ADMIN }