package co.edu.unicauca.urbanguard.data.remote.auth0

import android.util.Log
import co.edu.unicauca.urbanguard.BuildConfig
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
import kotlinx.coroutines.CancellationException
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
                continuation.resumeWithException(Auth0ErrorMapper.toAppException(error))
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
                    continuation.resumeWithException(Auth0ErrorMapper.toAppException(error))
                }
            })
    }

    /**
     * Persiste las credenciales de forma cifrada (Android Keystore). Si en la
     * respuesta viene un refresh_token (offline_access), la sesión podrá
     * restaurarse al reabrir la app.
     *
     * El catch amplio cubre la superficie de Android Keystore, que no es
     * enumerable. Perder la persistencia no debe impedir el inicio de sesión en
     * memoria, asi que se registra y se sigue.
     */
    @Suppress("TooGenericExceptionCaught")
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
     *
     * El catch amplio cubre la superficie de excepciones de
     * `CredentialsManager` y de Android Keystore, que no es enumerable. La
     * [CancellationException] se relanza aparte porque esta función es `suspend`:
     * sin esa guarda una restauración cancelada se disfrazaría de "no hay
     * sesión" y el usuario perdería el acceso sin motivo.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun restoreSession(): UserSession? {
        return try {
            if (!credentialsManager.hasValidCredentials()) {
                Log.d(TAG, "No hay credenciales válidas persistidas")
                return null
            }
            val credentials = credentialsManager.awaitCredentials()
            Log.d(TAG, "Sesión restaurada desde almacenamiento seguro")
            mapCredentialsToSession(credentials, fallbackEmail = "usuario@urbanguard.com")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar la sesión", e)
            null
        }
    }

    /** El catch amplio cubre la superficie de Android Keystore; limpiar es best-effort. */
    @Suppress("TooGenericExceptionCaught")
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
                // Si el ID token no puede decodificarse, mantener valores seguros.
                // Se acepta el catch amplio porque la entrada es un JWT no confiable
                // y la reaccion correcta es degradar a un usuario por defecto en
                // lugar de fallar el arranque de la app.
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
}
