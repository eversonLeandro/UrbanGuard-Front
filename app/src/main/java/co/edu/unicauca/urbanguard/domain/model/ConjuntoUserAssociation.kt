package co.edu.unicauca.urbanguard.domain.model

data class ConjuntoUserAssociation(
    val conjuntoId: String,
    val nombre: String,
    val roleEnConjunto: UserRole
)