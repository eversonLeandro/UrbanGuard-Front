package co.edu.unicauca.urbanguard.domain.model

/**
 * Errores tipados de la aplicación.
 *
 * Cada caso que el usuario puede facing es una clase concreta, no un texto.
 * Esto permite que la UI decida qué mostrar y que el mensaje se construya
 * una sola vez, en un único sitio, sin comparar cadenas.
 */
sealed class AppError {

    /** Texto final, ya redactado para el usuario. */
    abstract val message: String

    /** Error de validación de un campo del formulario. */
    data class Validation(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Revisa los datos ingresados e intenta de nuevo."
        }
    }

    /** El correo o la contraseña no coinciden. */
    data class InvalidCredentials(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Correo o contraseña incorrectos. Verifica tus datos."
        }
    }

    /** El correo ya tiene una cuenta creada. */
    data class EmailAlreadyRegistered(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Ya existe una cuenta registrada con este correo electrónico."
        }
    }

    /** La cuenta existe pero el correo aún no fue verificado. */
    data class EmailNotVerified(override val message: String) : AppError() {
        companion object {
            const val MESSAGE =
                "Tu cuenta aún no está verificada. Revisa el correo que enviamos y confirma tu cuenta."
        }
    }

    /**
     * La contraseña no cumple la política de seguridad.
     * [missingRules] describe en español lo que falta, para poder mostrarlo.
     */
    data class WeakPassword(
        override val message: String,
        val missingRules: List<String> = emptyList()
    ) : AppError() {
        companion object {
            const val MESSAGE = "La contraseña no cumple los requisitos de seguridad."
        }
    }

    /** Cuenta bloqueada por exceso de intentos fallidos. */
    data class AccountBlocked(override val message: String) : AppError() {
        companion object {
            const val MESSAGE =
                "Tu cuenta está bloqueada temporalmente por demasiados intentos fallidos. " +
                    "Espera unos minutos e inténtalo de nuevo."
        }
    }

    /** Cuenta inactiva o deshabilitada. */
    data class AccountDisabled(override val message: String) : AppError() {
        companion object {
            const val MESSAGE =
                "Esta cuenta está deshabilitada. Contacta al administrador de tu conjunto."
        }
    }

    /** Se requiere un segundo factor (MFA / Bot Protection). */
    data class ExtraVerificationRequired(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Se requiere una verificación adicional para continuar."
        }
    }

    /** El token venció o fue revocado: hay que volver a iniciar sesión. */
    data class SessionExpired(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Tu sesión expiró por seguridad. Inicia sesión de nuevo."
        }
    }

    /** Autenticado pero sin permisos para esta acción. */
    data class Forbidden(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "No tienes permiso para realizar esta acción."
        }
    }

    /** El recurso solicitado no existe. */
    data class NotFound(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "No encontramos la información que buscas."
        }
    }

    /** Conflicto con el estado actual: ya existe, ya fue procesado. */
    data class Conflict(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "No se puede completar la acción porque el estado ya cambió."
        }
    }

    /** Demasiadas solicitudes en poco tiempo. */
    data class RateLimited(override val message: String) : AppError() {
        companion object {
            const val MESSAGE =
                "Demasiados intentos seguidos. Espera un momento e intenta de nuevo."
        }
    }

    /** Sin conexión a internet. */
    data class NoConnection(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Sin conexión a internet. Revisa tu red e intenta de nuevo."
        }
    }

    /** Fallo del servidor o de un servicio intermedio. */
    data class ServerError(override val message: String) : AppError() {
        companion object {
            const val MESSAGE =
                "El servicio no está disponible en este momento. Intenta de nuevo en unos segundos."
        }
    }

    /** Cualquier otro fallo, sin detalle técnico para el usuario. */
    data class Unexpected(override val message: String) : AppError() {
        companion object {
            const val MESSAGE = "Ocurrió un error inesperado. Intenta de nuevo."
        }
    }

    companion object {
        /**
         * Resuelve una excepción recibida por la capa de UI.
         *
         * Los repositorios lanzan siempre un [AppException], así que en el caso normal
         * se devuelve el error de negocio ya traducido. Cualquier otra excepción
         * significa que el flujo no pasó por la capa de datos, y se degrada a
         * [Unexpected] en vez de mostrar un texto técnico al usuario.
         */
        fun from(throwable: Throwable): AppError =
            (throwable as? AppException)?.error ?: Unexpected(Unexpected.MESSAGE)
    }
}

/**
 * Excepción que transporta un [AppError].
 *
 * Los repositorios lanzan o envuelven fallos en esta excepción para que el
 * mensaje de negocio viaje intacto hasta la UI, sin que los capas superiores
 * lo vuelven a envolver ni le añadan texto genérico.
 */
class AppException(val error: AppError) : Exception(error.message)
