package co.edu.unicauca.urbanguard.data.remote.auth0

import android.util.Log
import co.edu.unicauca.urbanguard.BuildConfig
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.callback.Callback
import com.auth0.android.jwt.JWT
import com.auth0.android.result.Credentials
import com.auth0.android.result.DatabaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Singleton
class Auth0Manager @Inject constructor(
    private val apiClient: AuthenticationAPIClient,
    private val credentialsManager: SecureCredentialsManager
) {

    companion object {
        private const val TAG = "Auth0Manager"

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
    }

    suspend fun loginEmbedded(
        email: String,
        password: String,
        connection: String = BuildConfig.AUTH0_DB_CONNECTION
    ): UserSession {
        val credentials = authenticateDirect(email.trim(), password, connection)
        saveCredentials(credentials)
        return mapCredentialsToSession(credentials, fallbackEmail = email.trim())
    }

    suspend fun signUpEmbedded(
        email: String,
        password: String,
        name: String = "",
        connection: String = BuildConfig.AUTH0_DB_CONNECTION
    ): UserSession {
        // 1. Ejecutar el registro nativo de Auth0 (guarda el nombre en user_metadata)
        signUpDirect(email.trim(), password, name, connection)

        // 2. Iniciar sesión para obtener las credenciales y tokens JWT
        return loginEmbedded(email.trim(), password, connection)
    }

    private suspend fun signUpDirect(
        email: String,
        password: String,
        name: String,
        connection: String
    ): DatabaseUser = suspendCancellableCoroutine { continuation ->
        // user_metadata.name: lo usa la Post-Login Action y el espejo JIT del backend.
        val userMetadata = if (name.isBlank()) null else mapOf("name" to name)
        var request = apiClient.createUser(email, password, null, connection, userMetadata)
        // El atributo root "name" hace que Auth0 guarde el perfil con name = nombre escrito
        // (si no, Auth0 pone name = email), y así el claim "name" del ID token lleva el nombre.
        if (!name.isBlank()) {
            request = request.addParameter("name", name)
        }
        request.start(object : Callback<DatabaseUser, AuthenticationException> {
            override fun onSuccess(result: DatabaseUser) {
                Log.d(TAG, "Usuario creado exitosamente en Auth0: ${result.email}")
                continuation.resume(result)
            }

            override fun onFailure(error: AuthenticationException) {
                Log.e(
                    TAG,
                    "Error en registro de Auth0: code=${error.getCode()}, desc=${error.getDescription()}",
                    error
                )
                continuation.resumeWithException(mapAuth0Error(error))
            }
        })
    }

    private suspend fun authenticateDirect(
        email: String,
        password: String,
        connection: String
    ): Credentials = suspendCancellableCoroutine { continuation ->
        apiClient.login(email, password, connection)
            .setScope("openid profile email offline_access")
            .setAudience(BuildConfig.AUTH0_AUDIENCE)
            .validateClaims()
            .start(object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    Log.d(TAG, "Autenticación exitosa")
                    continuation.resume(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e(
                        TAG,
                        "Error en login de Auth0: code=${error.getCode()}, desc=${error.getDescription()}",
                        error
                    )
                    continuation.resumeWithException(mapAuth0Error(error))
                }
            })
    }

    /**
     * Persiste las credenciales de forma cifrada (Android Keystore). Si en la
     * respuesta viene un refresh_token (offline_access), la sesión podrá
     * restaurarse al reabrir la app.
     */
    private fun saveCredentials(credentials: Credentials) {
        try {
            credentialsManager.saveCredentials(credentials)
            Log.d(
                TAG,
                "Credenciales persistidas de forma segura (refresh=${credentials.refreshToken != null})"
            )
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron persistir las credenciales", e)
        }
    }

    /**
     * Restaura la sesión persistida al arrancar la app. Si el access token
     * expiró y hay refresh_token, Auth0 emite uno nuevo (rotación automática).
     * Devuelve null cuando no hay credenciales o ya no son renovables.
     */
    suspend fun restoreSession(): UserSession? {
        return try {
            if (!credentialsManager.hasValidCredentials()) {
                Log.d(TAG, "No hay credenciales válidas persistidas")
                return null
            }
            val credentials = credentialsManager.awaitCredentials()
            Log.d(TAG, "Sesión restaurada desde almacenamiento seguro")
            mapCredentialsToSession(credentials, fallbackEmail = "usuario@urbanguard.com")
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar la sesión", e)
            null
        }
    }

    fun clearStoredCredentials() {
        try {
            credentialsManager.clearCredentials()
            Log.d(TAG, "Credenciales persistidas eliminadas")
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron limpiar las credenciales", e)
        }
    }

    private fun mapCredentialsToSession(
        credentials: Credentials,
        fallbackEmail: String
    ): UserSession {
        val accessToken = credentials.accessToken
        val idTokenStr = credentials.idToken

        var userId = fallbackEmail
        var userName = fallbackEmail.substringBefore("@")
        var role = UserRole.USER

        if (!idTokenStr.isNullOrBlank()) {
            try {
                val jwt = JWT(idTokenStr)
                userId = jwt.subject ?: fallbackEmail

                val nameClaim = jwt.getClaim("https://urbanguard.com/name").asString()
                    ?: jwt.getClaim("name").asString()
                    ?: jwt.getClaim("nickname").asString()
                if (!nameClaim.isNullOrBlank()) {
                    userName = nameClaim
                }

                // Extraer rol de claims estándar o personalizados de Auth0 (alineado con la Action real)
                val rawRole = jwt.getClaim("https://urbanguard.com/rol").asString()
                    ?: jwt.getClaim("https://urbanguard.com/role").asString()
                    ?: jwt.getClaim("role").asString()
                    ?: jwt.getClaim("roles").asList(String::class.java)?.firstOrNull()

                // Claim de plataforma superadmin (capa 2.3)
                val staff = jwt.getClaim("https://urbanguard.com/staff").asBoolean() ?: false

                role = if (staff) UserRole.SUPERADMIN else parseUserRole(rawRole)

                Log.d(TAG, "=== [LOGIN AUTH0] DATOS DE SESION ===")
                Log.d(TAG, "  userId (sub): $userId")
                Log.d(TAG, "  userName: $userName")
                Log.d(TAG, "  staff (claim): $staff")
                Log.d(TAG, "  rawRole (claim): $rawRole")
                Log.d(TAG, "  role resuelto: $role")
                Log.d(TAG, "  token (prefijo): ${accessToken.take(25)}...")
                Log.d(TAG, "====================================")
            } catch (_: Exception) {
                // Si el ID token no puede decodificarse, mantener valores seguros
            }
        }

        return UserSession(
            token = accessToken,
            userId = userId,
            name = userName,
            role = role
        )
    }

    /**
     * Mapea el raw-claim `https://urbanguard.com/rol` a [UserRole].
     *
     * "USER" ya no es un rol de negocio en Auth0; Auth0 retorna [rawRole]=null cuando el
     * usuario no tiene rol asignado (no ha vinculado ningún conjunto). El centinela
     * [UserRole.USER] se produce solo por el `else`, y el NavGraph lo redirige a
     * [ConjuntoSelectionScreen].
     */
    private fun parseUserRole(rawRole: String?): UserRole {
        return when (rawRole?.trim()?.uppercase()) {
            "SUPERADMIN", "SUPER ADMINISTRADOR" -> UserRole.SUPERADMIN
            "ADMIN", "ADMINISTRADOR" -> UserRole.ADMIN
            "GUARD", "VIGILANTE", "PORTERO", "SEGURIDAD" -> UserRole.GUARD
            "RESIDENT", "RESIDENTE", "PROPIETARIO", "INQUILINO" -> UserRole.RESIDENT
            // Auth0 ya NO emite "USER" como rol de negocio.
            // Cualquier claim desconocido o null → centinela USER → ConjuntoSelection.
            else -> UserRole.USER
        }
    }

    /**
     * Traduce una [AuthenticationException] de Auth0 a un [AppError] con texto en español.
     *
     * Principio: al usuario se le explica qué hacer, nunca se le muestra la descripción
     * cruda de Auth0 (viene en inglés y en muchos casos es un error de configuración
     * interno, no un problema del usuario). El código real se registra en el log.
     *
     * Devuelve un [AppException] para que el mensaje de negocio viaje intacto hasta la
     * capa de UI: [co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper] respeta
     * los [AppException] y no los vuelve a envolver.
     */
    private fun mapAuth0Error(error: AuthenticationException): AppException {
        val code = error.getCode().orEmpty().trim().lowercase()
        val description = error.getDescription().orEmpty().trim()
        val mapped: AppError = when {
            // --- Registro duplicado -------------------------------------------------
            code == "user_exists" || description.contains("already exists", ignoreCase = true) ->
                AppError.EmailAlreadyRegistered(AppError.EmailAlreadyRegistered.MESSAGE)

            // --- Credenciales incorrectas ------------------------------------------
            error.isInvalidCredentials ||
                    code in setOf(
                "invalid_grant",
                "wrong_email_password",
                "invalid_user_password"
            ) ->
                AppError.InvalidCredentials(AppError.InvalidCredentials.MESSAGE)

            // --- Cuenta creada pero correo sin verificar ---------------------------
            code == "requires_verification" || error.isVerificationRequired ||
                    description.contains("not confirmed", ignoreCase = true) ||
                    description.contains("not been verified", ignoreCase = true) ->
                AppError.EmailNotVerified(AppError.EmailNotVerified.MESSAGE)

            // --- Política de contraseña --------------------------------------------
            error.isPasswordLeaked ->
                AppError.WeakPassword(
                    "Esa contraseña aparece en filtraciones de datos conocidas. Elige una nueva."
                )

            error.isPasswordAlreadyUsed ->
                AppError.WeakPassword("Esa contraseña ya la usaste antes. Elige una diferente.")

            error.isRuleError || error.isPasswordNotStrongEnough ||
                    code in setOf(
                "invalid_password",
                "password_strength_error",
                "password_dictionary_error"
            ) ->
                AppError.WeakPassword(PASSWORD_POLICY_MESSAGE)

            // --- Sesión vencida ----------------------------------------------------
            error.isInvalidRefreshToken || error.isRefreshTokenDeleted ||
                    code in setOf("expired_token", "invalid_refresh_token", "login_required") ->
                AppError.SessionExpired(AppError.SessionExpired.MESSAGE)

            // --- Verificación adicional (MFA / Bot Protection) ----------------------
            error.isMultifactorRequired || error.isMultifactorEnrollRequired ||
                    code in setOf(
                "mfa_required",
                "mfa_registration_required",
                "mfa_invalid_code"
            ) ->
                AppError.ExtraVerificationRequired(AppError.ExtraVerificationRequired.MESSAGE)

            // --- Cuenta bloqueada o inactiva ---------------------------------------
            error.isTooManyAttempts || code == "too_many_attempts" ->
                AppError.AccountBlocked(AppError.AccountBlocked.MESSAGE)

            error.isAccessDenied || code == "access_denied" ->
                AppError.AccountDisabled(AppError.AccountDisabled.MESSAGE)

            // --- Conectividad ------------------------------------------------------
            error.isNetworkError ->
                AppError.NoConnection(AppError.NoConnection.MESSAGE)

            // --- Configuración incorrecta del lado del proyecto --------------------
            // No son culpa del usuario: se avisa de forma genérica y se deja traza.
            error.isInvalidConfiguration || error.isIdTokenValidationError ||
                    code in setOf(
                "unauthorized_client",
                "invalid_request",
                "invalid_authorize_url"
            ) ->
                AppError.Unexpected(APP_CONFIG_MESSAGE)

            code == "public signup is disabled" || description.contains(
                "signup is disabled",
                ignoreCase = true
            ) ->
                AppError.Unexpected(SIGNUP_DISABLED_MESSAGE)

            // --- Sin código reconocible --------------------------------------------
            else -> {
                Log.w(TAG, "Código de Auth0 sin mapeo: code=$code, desc=$description")
                AppError.Unexpected(AppError.Unexpected.MESSAGE)
            }
        }

        Log.e(TAG, "Auth0 rechazado: code=$code, tipo=${error.javaClass.simpleName}")
        return AppException(mapped)
    }
}
