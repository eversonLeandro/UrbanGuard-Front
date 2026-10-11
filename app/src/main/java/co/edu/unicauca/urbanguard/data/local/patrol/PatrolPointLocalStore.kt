package co.edu.unicauca.urbanguard.data.local.patrol

import android.content.Context
import android.util.Log
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointResponseDto
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda los puntos de control por conjunto en almacenamiento cifrado.
 *
 * No hay dependencia de Room en el proyecto, así que se reutiliza el mismo
 * mecanismo cifrado de la sesión (Auth0 `SharedPreferencesStorage`) con un
 * archivo propio: la lista viaja como JSON bajo la clave del conjunto. Cuando
 * el backend esté listo, esta clase sigue como caché sin cambiar su firma.
 */
@Singleton
class PatrolPointLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val storage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(context, PREFS_NAME)
    }
    private val gson = Gson()

    /** Puntos del conjunto, o lista vacía si aún no hay ninguno. */
    @Suppress("TooGenericExceptionCaught")
    fun load(complexId: String): List<PatrolPointResponseDto> {
        return try {
            val raw = storage.retrieveString(keyFor(complexId)) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            val type = object : TypeToken<List<PatrolPointResponseDto>>() {}.type
            gson.fromJson<List<PatrolPointResponseDto>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer los puntos de control", e)
            emptyList()
        }
    }

    /** Reemplaza los puntos del conjunto. */
    @Suppress("TooGenericExceptionCaught")
    fun save(complexId: String, points: List<PatrolPointResponseDto>) {
        try {
            storage.store(keyFor(complexId), gson.toJson(points))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron guardar los puntos de control", e)
        }
    }

    private fun keyFor(complexId: String): String = "$KEY_PREFIX$complexId"

    private companion object {
        const val TAG = "PatrolStore"
        const val PREFS_NAME = "urbanguard_patrol_points"
        const val KEY_PREFIX = "points_"
    }
}
