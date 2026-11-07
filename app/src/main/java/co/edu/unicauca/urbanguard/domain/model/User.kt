package co.edu.unicauca.urbanguard.domain.model

/**
 * Cuenta de usuario gestionada por identity-service (HU-006).
 *
 * Espejo local de la cuenta, sincronizado desde la fuente de verdad (Auth0):
 * desactivar bloquea primero en Auth0 (`blocked=true`) y después marca la fila
 * con `activo=false`; reactivar desbloquea siempre y sincroniza después. El
 * historial nunca se elimina: la fila conserva los datos con su marca.
 *
 * [userId] es el `sub` de Auth0, no el UUID interno: con él se compara la fila
 * propia del superadministrador para impedir la autodesactivación.
 */
data class User(
    val publicId: String,
    val userId: String,
    val email: String?,
    val nombres: String?,
    val apellidos: String?,
    val activo: Boolean,
    val createdAt: String?,
    val updatedAt: String?
) {

    /**
     * Nombre visible con el mimo del backend (`name` = "nombres apellidos"):
     * cae por partes hasta email, de modo que la fila nunca queda vacía.
     */
    val displayName: String
        get() = listOfNotNull(nombres?.trim(), apellidos?.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .ifEmpty { email?.trim().orEmpty() }
            .ifEmpty { "Usuario sin datos" }
}
