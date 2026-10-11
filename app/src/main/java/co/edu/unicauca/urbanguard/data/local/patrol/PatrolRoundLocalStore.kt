package co.edu.unicauca.urbanguard.data.local.patrol

import android.content.Context
import android.util.Log
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las rondas del guardia por conjunto en almacenamiento cifrado.
 *
 * Es la constancia local del turno: cada lectura queda con su hora aunque no
 * haya red. Cuando el backend esté listo, esta clase sigue como caché.
 */
@Singleton
class PatrolRoundLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val storage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(context, PREFS_NAME)
    }
    private val gson = Gson()

    /** Rondas del conjunto, más recientes primero. */
    @Suppress("TooGenericExceptionCaught")
    fun load(complexId: String): List<PatrolRoundDto> {
        return try {
            val raw = storage.retrieveString(keyFor(complexId)) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            val type = object : TypeToken<List<PatrolRoundDto>>() {}.type
            gson.fromJson<List<PatrolRoundDto>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer las rondas", e)
            emptyList()
        }
    }

    /** Reemplaza las rondas del conjunto. */
    @Suppress("TooGenericExceptionCaught")
    fun save(complexId: String, rounds: List<PatrolRoundDto>) {
        try {
            storage.store(keyFor(complexId), gson.toJson(rounds))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron guardar las rondas", e)
        }
    }

    private fun keyFor(complexId: String): String = "$KEY_PREFIX$complexId"

    private companion object {
        const val TAG = "PatrolRoundStore"
        const val PREFS_NAME = "urbanguard_patrol_rounds"
        const val KEY_PREFIX = "rounds_"
    }
}
