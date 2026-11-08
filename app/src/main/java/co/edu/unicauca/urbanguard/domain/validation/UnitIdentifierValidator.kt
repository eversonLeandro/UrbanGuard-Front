package co.edu.unicauca.urbanguard.domain.validation

/**
 * Reglas compartidas para los identificadores de apartamento.
 *
 * Deben coincidir con `Unit.IDENTIFIER_REGEX` y `Unit.MAX_IDENTIFIER_LENGTH`
 * del community-service para que el usuario corrija el valor antes de enviarlo.
 */
object UnitIdentifierValidator {

    const val MAX_LENGTH = 80

    private val VALID_IDENTIFIER = Regex("^[\\p{L}\\p{N} -]+$")
    private val INVALID_CHARACTERS = Regex("[^\\p{L}\\p{N} -]")

    fun validate(value: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> EMPTY_MESSAGE
            trimmed.length > MAX_LENGTH -> TOO_LONG_MESSAGE
            !VALID_IDENTIFIER.matches(trimmed) -> INVALID_CHARACTERS_MESSAGE
            else -> null
        }
    }

    fun sanitize(value: String): String =
        INVALID_CHARACTERS.replace(value, "").take(MAX_LENGTH)

    private const val EMPTY_MESSAGE = "Ingresa el número o identificador del apartamento."
    private const val TOO_LONG_MESSAGE =
        "El número o identificador no puede exceder $MAX_LENGTH caracteres."
    private const val INVALID_CHARACTERS_MESSAGE =
        "Solo se permiten letras, números, espacios y guion."
}
