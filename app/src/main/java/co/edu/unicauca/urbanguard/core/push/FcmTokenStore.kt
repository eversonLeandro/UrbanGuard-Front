package co.edu.unicauca.urbanguard.core.push

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda local el último token FCM conocido (no es secreto). Permite
 * desregistrarlo al cerrar sesión aunque Firebase rote el token con la app
 * cerrada, y reintentar el registro cuando haya conjunto activo.
 */
@Singleton
class FcmTokenStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val PREFS = "fcm_token_store"
        const val KEY_TOKEN = "fcm_token"
    }
}
