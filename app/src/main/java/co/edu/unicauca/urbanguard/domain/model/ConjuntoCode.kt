package co.edu.unicauca.urbanguard.domain.model

data class ConjuntoCode(
    val code: String,           // ej: "URB-4X9K"
    val isActive: Boolean,
    val conjuntoName: String    // TODO: eliminar este campo cuando el registro
                                // del conjunto esté implementado (UG-45/UG-46).
                                // El nombre vendrá del modelo ConjuntoConfig
                                // que se cargará desde el repositorio correspondiente.)
)
