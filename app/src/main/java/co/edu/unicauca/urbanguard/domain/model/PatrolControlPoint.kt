package co.edu.unicauca.urbanguard.domain.model

/**
 * Punto de control NFC de un conjunto.
 *
 * El `nfcTagId` es el identificador físico del tag (hex en mayúsculas,
 * p. ej. "04A3F21B8C50"), leído con el lector del equipo al registrar.
 * El orden de la ruta no vive aquí: pertenece a [PatrolRoute.pointIds].
 */
data class PatrolControlPoint(
    val id: String,
    val complexId: String,
    val name: String,
    val location: String,
    val description: String = "",
    val nfcTagId: String,
    val isActive: Boolean
)

/** Resultado de validar el formulario de un punto de control. */
enum class PatrolPointValidation {
    VALID,
    NAME_REQUIRED,
    NAME_TOO_LONG,
    TAG_REQUIRED,
    TAG_INVALID
}

private const val MAX_NAME_LENGTH = 100
private val NFC_UID_LENGTHS = setOf(8, 14, 20)
private val HEX_PATTERN = Regex("^[0-9A-F]+$")

/**
 * Valida los datos del formulario. El tag es obligatorio porque sin tag
 * físico el punto no sirve para la ronda.
 */
fun validatePatrolPoint(name: String, nfcTagId: String): PatrolPointValidation {
    if (name.isBlank()) return PatrolPointValidation.NAME_REQUIRED
    if (name.trim().length > MAX_NAME_LENGTH) return PatrolPointValidation.NAME_TOO_LONG
    if (nfcTagId.isBlank()) return PatrolPointValidation.TAG_REQUIRED
    if (!isValidNfcUid(nfcTagId)) return PatrolPointValidation.TAG_INVALID
    return PatrolPointValidation.VALID
}

/**
 * Quita separadores y normaliza a mayúsculas. El backend acepta UID con
 * `:` o `-`; para comparar tags localmente siempre usamos la forma compacta.
 */
fun normalizeNfcUid(raw: String): String =
    raw.trim().uppercase().replace(":", "").replace("-", "").replace(" ", "")

/** El backend acepta UID de 4, 7 u 10 bytes en hexadecimal. */
fun isValidNfcUid(raw: String): Boolean {
    val normalized = normalizeNfcUid(raw)
    return normalized.length in NFC_UID_LENGTHS && HEX_PATTERN.matches(normalized)
}

/** Lista de puntos activos primero, luego por nombre. */
fun List<PatrolControlPoint>.sortedForRoute(): List<PatrolControlPoint> =
    sortedWith(compareBy({ !it.isActive }, { it.name }))
