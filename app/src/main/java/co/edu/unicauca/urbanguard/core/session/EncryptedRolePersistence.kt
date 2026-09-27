package co.edu.unicauca.urbanguard.core.session

import android.content.Context
import android.util.Log
import co.edu.unicauca.urbanguard.domain.model.UserRole
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste el rol de sesion en SharedPreferences cifradas usando el mismo
 * mecanismo y keystore que las credenciales de Auth0, pero en un archivo
 * independiente para no colisionar claves con el storage de credenciales.
 *
 * Solo guarda metadatos de navegacion (id de usuario, rol y conjunto activo),
 * nunca el access token: ese sigue a cargo de [co.edu.unicauca.urbanguard.data.remote.auth0.Auth0Manager].
 */
@Singleton
class EncryptedRolePersistence @Inject constructor(
    @ApplicationContext context: Context
) : RolePersistence {

    private val storage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(context, PREFS_NAME)
    }

    /**
     * El catch amplio cubre la superficie de excepciones de Android Keystore y de
     * AndroidX Security (GeneralSecurityException, IOException, IllegalStateException,
     * SecurityException), que no es enumerable ni estable entre versiones de Android.
     * El rol persistido es una copia cacheada: si falla, la sesion sigue viva en
     * memoria y solo se pierde la restauracion del rol al reabrir la app.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun save(persisted: PersistedRole) {
        try {
            storage.store(KEY_USER_ID, persisted.userId)
            storage.store(KEY_ROLE, persisted.role.name)
            val conjuntoActivoId = persisted.conjuntoActivoId
            if (conjuntoActivoId == null) {
                storage.remove(KEY_CONJUNTO_ACTIVO)
                storage.remove(KEY_CONJUNTO_ACTIVO_NOMBRE)
            } else {
                storage.store(KEY_CONJUNTO_ACTIVO, conjuntoActivoId)
                val nombre = persisted.conjuntoActivoNombre
                if (nombre.isNullOrBlank()) {
                    storage.remove(KEY_CONJUNTO_ACTIVO_NOMBRE)
                } else {
                    storage.store(KEY_CONJUNTO_ACTIVO_NOMBRE, nombre)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo persistir el rol de sesion", e)
        }
    }

    /**
     * @see save para la justificacion del catch amplio.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun read(): PersistedRole? {
        return try {
            val userId =
                storage.retrieveString(KEY_USER_ID)?.takeIf { it.isNotBlank() } ?: return null
            val role = storage.retrieveString(KEY_ROLE)
                ?.let { raw -> UserRole.entries.firstOrNull { it.name == raw } }
                ?: return null
            PersistedRole(
                userId = userId,
                role = role,
                conjuntoActivoId = storage.retrieveString(KEY_CONJUNTO_ACTIVO),
                conjuntoActivoNombre = storage.retrieveString(KEY_CONJUNTO_ACTIVO_NOMBRE)
            )
        } catch (e: Exception) {
            // Keystore corrupto o datos cifrados de otra clave: se descarta la copia.
            Log.w(TAG, "No se pudo leer el rol de sesion persistido", e)
            null
        }
    }

    /**
     * @see save para la justificacion del catch amplio.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun clear() {
        try {
            storage.remove(KEY_USER_ID)
            storage.remove(KEY_ROLE)
            storage.remove(KEY_CONJUNTO_ACTIVO)
            storage.remove(KEY_CONJUNTO_ACTIVO_NOMBRE)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo limpiar el rol de sesion persistido", e)
        }
    }

    private companion object {
        const val TAG = "RolePersistence"
        const val PREFS_NAME = "urbanguard_session_prefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_ROLE = "role"
        const val KEY_CONJUNTO_ACTIVO = "conjunto_activo_id"
        const val KEY_CONJUNTO_ACTIVO_NOMBRE = "conjunto_activo_nombre"
    }
}
