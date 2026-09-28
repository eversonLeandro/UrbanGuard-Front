package co.edu.unicauca.urbanguard.domain.validation

/**
 * Validaciones de entrada compartidas.
 *
 * Se centralizan aquí para que el registro y el inicio de sesión apliquen
 * exactamente las mismas reglas: si el registro acepta algo que el login
 * rechaza (o al revés), el usuario percibe la app como inconsistente.
 *
 * Las reglas de contraseña coinciden con las que se comunican al usuario cuando
 * Auth0 rechaza el registro, para poder corregir antes de llamar al servidor.
 */
object InputValidators {

    private const val PASSWORD_MIN_LENGTH = 8

    /**
     * Formato de correo: parte local, arroba y dominio con punto y extensión.
     *
     * Se usa una expresión regular propia en lugar de `android.util.Patterns` porque
     * esta última es una clase del framework que no está disponible en las pruebas
     * unitarias de JVM, y porque su patrón acepta `a@b` (dominio sin punto), que es
     * justo uno de los casos que se quería rechazar.
     */
    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /**
     * Valida el formato de un correo.
     *
     * Antes se usaba `contains("@")`, que aceptaba entradas inválidas como `a@b`,
     * `@@` o `correo@`.
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.length <= MAX_EMAIL_LENGTH && EMAIL_REGEX.matches(trimmed)
    }

    private const val MAX_EMAIL_LENGTH = 254

    /**
     * Formatos de teléfono, de nombre de persona y longitudes máximas del
     * formulario de alta de vigilante.
     *
     * Deben calcar exactamente las restricciones de `CreateGuardRequest` en el
     * community-service (`@Size(max = 100)`, `@Size(max = 30)` y
     * `@Pattern(regexp = "^[+0-9()\\- ]*$")`). El motivo de duplicarlas aquí y no
     * solo avisar tras el 400 es que ese endpoint devuelve todos los fallos de
     * validación en un único string aplanado, sin un error por campo: sin reglas
     * locales el usuario solo descubre que el teléfono tenía letras después de
     * un viaje de ida y vuelta al servidor.
     */
    private val PHONE_REGEX = Regex("^[+0-9()\\- ]*$")

    const val PHONE_MAX_LENGTH = 30
    const val NAME_MAX_LENGTH = 100

    /**
     * Valida un teléfono de contacto.
     *
     * Admite dígitos, `+`, paréntesis, guion y espacios porque son los formatos
     * que se escriben a mano en pantalla; el backend usa el mismo conjunto.
     */
    fun isValidPhone(phone: String): Boolean {
        val trimmed = phone.trim()
        return trimmed.isNotEmpty() &&
            trimmed.length <= PHONE_MAX_LENGTH &&
            PHONE_REGEX.matches(trimmed)
    }

    /** Valida un nombre o apellido: no vacío y dentro del máximo del backend. */
    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= NAME_MAX_LENGTH
    }

    /**
     * Devuelve, en español y en el orden en que el usuario debe corregirlas,
     * las reglas de contraseña que no se cumplen. Vacía significa que es válida.
     */
    fun passwordPolicyViolations(password: String): List<String> = buildList {
        if (password.length < PASSWORD_MIN_LENGTH) {
            add("debe tener al menos $PASSWORD_MIN_LENGTH caracteres")
        }
        if (password.none { it.isUpperCase() }) {
            add("debe incluir al menos una letra mayúscula")
        }
        if (password.none { it.isDigit() }) {
            add("debe incluir al menos un número")
        }
    }

    /** Une las reglas incumplidas en un solo mensaje legible. */
    fun passwordPolicyMessage(password: String): String? {
        val violations = passwordPolicyViolations(password)
        if (violations.isEmpty()) return null
        return "La contraseña ${violations.joinToString(" y ")}."
    }
}
