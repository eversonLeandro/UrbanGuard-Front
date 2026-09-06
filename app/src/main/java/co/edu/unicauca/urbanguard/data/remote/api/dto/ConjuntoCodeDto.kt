package co.edu.unicauca.urbanguard.data.remote.api.dto

data class ConjuntoCodeResponseDto(
    val code: String,
    val isActive: Boolean,
    val conjuntoName: String    // TODO: eliminar cuando el backend devuelva el nombre
                                // desde el endpoint de configuración del conjunto (UG-46).
                                 // Por ahora el backend lo incluye en esta respuesta.
)
