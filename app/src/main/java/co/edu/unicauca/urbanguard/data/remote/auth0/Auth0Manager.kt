package co.edu.unicauca.urbanguard.data.remote.auth0

import android.util.Log
import co.edu.unicauca.urbanguard.BuildConfig
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
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
    private val apiClient: AuthenticationAPIClient
) {

    companion object {
        private const val TAG = "Auth0Manager"
    }

    suspend fun loginEmbedded(
        email: String,
        password: String,
        connection: String = BuildConfig.AUTH0_DB_CONNECTION
    ): UserSession {
        val credentials = authenticateDirect(email.trim(), password, connection)
        return mapCredentialsToSession(credentials, fallbackEmail = email.trim())
    }

    suspend fun signUpEmbedded(
        email: String,
        password: String,
        name: String = "",
        connection: String = BuildConfig.AUTH0_DB_CONNECTION
    ): UserSession {
        // 1. Ejecutar el registro nativo de Auth0
        signUpDirect(email.trim(), password, connection)

        // 2. Iniciar sesión para obtener las credenciales y tokens JWT
        return loginEmbedded(email.trim(), password, connection)
    }

    private suspend fun signUpDirect(
        email: String,
        password: String,
        connection: String
    ): DatabaseUser = suspendCancellableCoroutine { continuation ->
        apiClient.createUser(email, password, null, connection)
            .start(object : Callback<DatabaseUser, AuthenticationException> {
                override fun onSuccess(result: DatabaseUser) {
                    Log.d(TAG, "Usuario creado exitosamente en Auth0: ${result.email}")
                    continuation.resume(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Error en registro de Auth0: code=${error.getCode()}, desc=${error.getDescription()}", error)
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
            .setScope("openid profile email")
            .setAudience(BuildConfig.AUTH0_AUDIENCE)
            .validateClaims()
            .start(object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    Log.d(TAG, "Autenticación exitosa")
                    continuation.resume(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Error en login de Auth0: code=${error.getCode()}, desc=${error.getDescription()}", error)
                    continuation.resumeWithException(mapAuth0Error(error))
                }
            })
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

                val nameClaim = jwt.getClaim("name").asString()
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

    private fun mapAuth0Error(error: AuthenticationException): Exception {
        val code = error.getCode()
        val desc = error.getDescription()
        val message = when {
            code == "public signup is disabled" || desc?.contains("signup is disabled", ignoreCase = true) == true ->
                "El registro público de usuarios está deshabilitado en Auth0. Activa los registros en el Dashboard."
            code == "user_exists" || desc?.contains("already exists", ignoreCase = true) == true ->
                "Ya existe una cuenta registrada con este correo electrónico."
            error.isInvalidCredentials || code == "invalid_grant" || code == "wrong_email_password" ->
                "Correo o contraseña incorrectos. Verifica tus datos."
            error.isPasswordLeaked ->
                "Tu contraseña ha sido comprometida según las políticas de seguridad. Por favor cámbiala."
            code == "password_dictionary_error" || code == "password_strength_error" || code == "invalid_password" ->
                desc ?: "La contraseña no cumple con los requisitos de seguridad de Auth0 (verifica longitud, mayúsculas y números)."
            error.isAccessDenied || code == "access_denied" || code == "unauthorized" ->
                "Acceso denegado. Tu cuenta puede estar inactiva o bloqueada."
            error.isNetworkError ->
                "No se pudo conectar con el servidor de autenticación. Verifica tu conexión a internet."
            else ->
                desc ?: error.message ?: "Ocurrió un error de autenticación. Intenta de nuevo."
        }
        return Exception(message)
    }
}
