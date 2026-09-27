package co.edu.unicauca.urbanguard.ui.features.admin

import co.edu.unicauca.urbanguard.domain.model.CreatedGuard

/**
 * Estado del formulario de alta de vigilante.
 *
 * Los cuatro campos viven aquí y no en `remember` local de la pantalla: es lo
 * que permite que el ViewModel los valide y que la pantalla sobreviva a una
 * recreación de la Activity sin perder lo escrito.
 *
 * Los errores por campo ([emailError] y hermanos) son de origen local; el
 * [errorMessage] es el banner para todo lo que no se puede atribuir a un campo
 * concreto — 403, 404, 409, los 5xx y el 400 del backend.
 */
data class RegisterGuardUiState(
    val email: String = "",
    val emailError: String? = null,

    val nombres: String = "",
    val nombresError: String? = null,

    val apellidos: String = "",
    val apellidosError: String? = null,

    val telefono: String = "",
    val telefonoError: String? = null,

    // Evita el doble envío: el backend no es idempotente en el alta de la
    // membresía y el segundo POST competiría por el mismo par usuario/conjunto.
    val isSubmitting: Boolean = false,

    val errorMessage: String? = null,

    // No nulo cuando el 201 ya llegó: la pantalla lo pinta como diálogo de
    // éxito y el botón de regreso lo descarta.
    val createdGuard: CreatedGuard? = null
)
