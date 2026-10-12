package co.edu.unicauca.urbanguard.data.local.patrol

import android.content.Context
import android.util.Log
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las rutas de patrullaje por conjunto en almacenamiento cifrado.
 *
 * Misma técnica que [PatrolPointLocalStore]: sin Room en el proyecto, la
 * lista viaja como JSON bajo la clave del conjunto.
 */
@Singleton
class PatrolRouteLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val storage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(context, PREFS_NAME)
    }
    private val gson = Gson()

    /** Rutas del conjunto, o lista vacía si aún no hay ninguna. */
    @Suppress("TooGenericExceptionCaught")
    fun load(complexId: String): List<PatrolRouteDto> {
        return try {
            val raw = storage.retrieveString(keyFor(complexId)) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            val type = object : TypeToken<List<PatrolRouteDto>>() {}.type
            gson.fromJson<List<PatrolRouteDto>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer las rutas de patrullaje", e)
            emptyList()
        }
    }

    /** Reemplaza las rutas del conjunto. */
    @Suppress("TooGenericExceptionCaught")
    fun save(complexId: String, routes: List<PatrolRouteDto>) {
        try {
            storage.store(keyFor(complexId), gson.toJson(routes))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron guardar las rutas de patrullaje", e)
        }
    }

    private fun keyFor(complexId: String): String = "$KEY_PREFIX$complexId"

    private companion object {
        const val TAG = "PatrolRouteStore"
        const val PREFS_NAME = "urbanguard_patrol_routes"
        const val KEY_PREFIX = "routes_"
    }
}
