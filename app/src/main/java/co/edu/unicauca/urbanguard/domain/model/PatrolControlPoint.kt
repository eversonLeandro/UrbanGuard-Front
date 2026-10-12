package co.edu.unicauca.urbanguard.domain.model

/**
 * Punto de control NFC de la ruta de patrullaje de un conjunto.
 *
 * El `nfcTagId` es el identificador físico del tag (hex en mayúsculas,
 * p. ej. "04A3F21B8C50"), leído con el lector del equipo al registrar.
 * El `order` define la posición en la ronda (0 = primera parada).
 */
data class PatrolControlPoint(
    val id: String,
    val complexId: String,
    val name: String,
    val location: String,
    val nfcTagId: String,
    val order: Int,
    val isActive: Boolean
)

/** Resultado de validar el formulario de un punto de control. */
enum class PatrolPointValidation {
    VALID,
    NAME_REQUIRED,
    NAME_TOO_LONG,
    TAG_REQUIRED
}

private const val MAX_NAME_LENGTH = 100

/**
 * Valida los datos del formulario. El tag es obligatorio porque sin tag
 * físico el punto no sirve para la ronda.
 */
fun validatePatrolPoint(name: String, nfcTagId: String): PatrolPointValidation {
    if (name.isBlank()) return PatrolPointValidation.NAME_REQUIRED
    if (name.trim().length > MAX_NAME_LENGTH) return PatrolPointValidation.NAME_TOO_LONG
    if (nfcTagId.isBlank()) return PatrolPointValidation.TAG_REQUIRED
    return PatrolPointValidation.VALID
}

/** Orden de la ronda: por `order`, con los inactivos siempre al final. */
fun List<PatrolControlPoint>.sortedForRoute(): List<PatrolControlPoint> =
    sortedWith(compareBy({ !it.isActive }, { it.order }, { it.name }))
