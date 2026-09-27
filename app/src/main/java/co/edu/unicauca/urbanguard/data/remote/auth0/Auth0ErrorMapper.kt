package co.edu.unicauca.urbanguard.data.remote.auth0

import android.util.Log
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import com.auth0.android.authentication.AuthenticationException

/**
 * Traduce una [AuthenticationException] de Auth0 a un [AppException] con texto en
 * español.
 *
 * Principio: al usuario se le explica qué hacer, nunca se le muestra la
 * descripción cruda de Auth0 (viene en inglés y en muchos casos es un error de
 * configuración interno, no un problema del usuario). El código real se registra
 * en el log.
 *
 * Devuelve un [AppException] para que el mensaje de negocio viaje intacto hasta
 * la capa de UI: [co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper]
 * respeta los [AppException] y no los vuelve a envolver.
 *
 * Vive aparte de [Auth0Manager] porque son preocupaciones distintas: el manager
 * maneja el ciclo de vida de credenciales y sesión, y este objeto solo traduce
 * errores. Además [Auth0Manager] ya está al tope de funciones por archivo.
 *
 * El mapeo se resuelve como una cadena de grupos y no como una sola decisión:
 * cada grupo devuelve `null` cuando no aplica, y el primero que coincide gana.
 * El orden de los grupos es significativo porque Auth0 puede describir más de
 * una cosa a la vez, y hay que ganar siempre por la más específica para el
 * usuario.
 */
internal object Auth0ErrorMapper {

    private const val TAG = "Auth0ErrorMapper"

    /**
     * Política de contraseña que la app exige. Se muestra completa para que el
     * usuario sepa qué corregir, en lugar de devolverle un error en inglés.
     */
    private const val PASSWORD_POLICY_MESSAGE =
        "La contraseña no cumple la política de seguridad. " +
                "Usa al menos 8 caracteres, con al menos una mayúscula y un número."

    private const val APP_CONFIG_MESSAGE =
        "La aplicación no pudo autenticarte en este momento. Intenta de nuevo en unos minutos."

    private const val SIGNUP_DISABLED_MESSAGE =
        "El registro de cuentas nuevas no está disponible en este momento. " +
                "Contacta al administrador de tu conjunto."

    private const val LEAKED_PASSWORD_MESSAGE =
        "Esa contraseña aparece en filtraciones de datos conocidas. Elige una nueva."

    private const val REUSED_PASSWORD_MESSAGE =
        "Esa contraseña ya la usaste antes. Elige una diferente."

    // Los códigos de Auth0 son la fuente más estable para clasificar: el texto
    // de la descripción cambia entre versiones del SDK, el código no.
    private val CREDENTIAL_ERROR_CODES =
        setOf("invalid_grant", "wrong_email_password", "invalid_user_password")

    private val WEAK_PASSWORD_CODES =
        setOf("invalid_password", "password_strength_error", "password_dictionary_error")

    private val SESSION_EXPIRED_CODES =
        setOf("expired_token", "invalid_refresh_token", "login_required")

    private val MFA_CODES =
        setOf("mfa_required", "mfa_registration_required", "mfa_invalid_code")

    private val PROJECT_CONFIG_CODES =
        setOf("unauthorized_client", "invalid_request", "invalid_authorize_url")

    fun toAppException(error: AuthenticationException): AppException {
        val code = error.getCode().orEmpty().trim().lowercase()
        val description = error.getDescription().orEmpty().trim()

        val mapped = credentialError(error, code, description)
            ?: passwordPolicyError(error, code)
            ?: sessionError(error, code)
            ?: accountStateError(error, code)
            ?: infrastructureError(error, code, description)
            ?: unmappedError(code, description)

        Log.e(TAG, "Auth0 rechazado: code=$code, tipo=${error.javaClass.simpleName}")
        return AppException(mapped)
    }

    /** Registro duplicado, credenciales incorrectas y correo sin verificar. */
    private fun credentialError(
        error: AuthenticationException,
        code: String,
        description: String
    ): AppError? = when {
        code == "user_exists" || description.contains("already exists", ignoreCase = true) ->
            AppError.EmailAlreadyRegistered(AppError.EmailAlreadyRegistered.MESSAGE)

        error.isInvalidCredentials || code in CREDENTIAL_ERROR_CODES ->
            AppError.InvalidCredentials(AppError.InvalidCredentials.MESSAGE)

        code == "requires_verification" || error.isVerificationRequired ||
                description.contains("not confirmed", ignoreCase = true) ||
                description.contains("not been verified", ignoreCase = true) ->
            AppError.EmailNotVerified(AppError.EmailNotVerified.MESSAGE)

        else -> null
    }

    /**
     * Contraseña filtrada, reutilizada o que no cumple la política. Cada caso
     * tiene su propio texto porque la acción que debe tomar el usuario cambia.
     */
    private fun passwordPolicyError(
        error: AuthenticationException,
        code: String
    ): AppError? = when {
        error.isPasswordLeaked ->
            AppError.WeakPassword(LEAKED_PASSWORD_MESSAGE)

        error.isPasswordAlreadyUsed ->
            AppError.WeakPassword(REUSED_PASSWORD_MESSAGE)

        error.isRuleError || error.isPasswordNotStrongEnough || code in WEAK_PASSWORD_CODES ->
            AppError.WeakPassword(PASSWORD_POLICY_MESSAGE)

        else -> null
    }

    /** Sesión vencida y verificación adicional pendiente (MFA / Bot Protection). */
    private fun sessionError(
        error: AuthenticationException,
        code: String
    ): AppError? = when {
        error.isInvalidRefreshToken || error.isRefreshTokenDeleted ||
                code in SESSION_EXPIRED_CODES ->
            AppError.SessionExpired(AppError.SessionExpired.MESSAGE)

        error.isMultifactorRequired || error.isMultifactorEnrollRequired ||
                code in MFA_CODES ->
            AppError.ExtraVerificationRequired(AppError.ExtraVerificationRequired.MESSAGE)

        else -> null
    }

    /** Cuenta bloqueada por intentos o desactivada por el administrador. */
    private fun accountStateError(
        error: AuthenticationException,
        code: String
    ): AppError? = when {
        error.isTooManyAttempts || code == "too_many_attempts" ->
            AppError.AccountBlocked(AppError.AccountBlocked.MESSAGE)

        error.isAccessDenied || code == "access_denied" ->
            AppError.AccountDisabled(AppError.AccountDisabled.MESSAGE)

        else -> null
    }

    /**
     * Conectividad y configuración del lado del proyecto. Los dos casos de
     * configuración no son culpa del usuario: se avisa de forma genérica.
     */
    private fun infrastructureError(
        error: AuthenticationException,
        code: String,
        description: String
    ): AppError? = when {
        error.isNetworkError ->
            AppError.NoConnection(AppError.NoConnection.MESSAGE)

        error.isInvalidConfiguration || error.isIdTokenValidationError ||
                code in PROJECT_CONFIG_CODES ->
            AppError.Unexpected(APP_CONFIG_MESSAGE)

        code == "public signup is disabled" || description.contains(
            "signup is disabled",
            ignoreCase = true
        ) ->
            AppError.Unexpected(SIGNUP_DISABLED_MESSAGE)

        else -> null
    }

    /** Último recurso: deja traza del código sin mapeo para poder diagnosticarlo. */
    private fun unmappedError(code: String, description: String): AppError {
        Log.w(TAG, "Código de Auth0 sin mapeo: code=$code, desc=$description")
        return AppError.Unexpected(AppError.Unexpected.MESSAGE)
    }
}
