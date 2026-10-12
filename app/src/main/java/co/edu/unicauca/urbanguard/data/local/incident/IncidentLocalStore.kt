package co.edu.unicauca.urbanguard.data.local.incident

import android.content.Context
import android.util.Log
import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las novedades por conjunto en almacenamiento cifrado.
 *
 * Misma técnica que puntos, rutas y rondas: la lista viaja como JSON bajo
 * la clave del conjunto. Las fotos viven como archivos propios (ver
 * `core.camera`) y aquí solo se guarda su ruta.
 */
@Singleton
class IncidentLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val storage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(context, PREFS_NAME)
    }
    private val gson = Gson()

    /** Novedades del conjunto, más recientes primero. */
    @Suppress("TooGenericExceptionCaught")
    fun load(complexId: String): List<IncidentResponseDto> {
        return try {
            val raw = storage.retrieveString(keyFor(complexId)) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            val type = object : TypeToken<List<IncidentResponseDto>>() {}.type
            gson.fromJson<List<IncidentResponseDto>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer las novedades", e)
            emptyList()
        }
    }

    /** Reemplaza las novedades del conjunto. */
    @Suppress("TooGenericExceptionCaught")
    fun save(complexId: String, incidents: List<IncidentResponseDto>) {
        try {
            storage.store(keyFor(complexId), gson.toJson(incidents))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron guardar las novedades", e)
        }
    }

    private fun keyFor(complexId: String): String = "$KEY_PREFIX$complexId"

    private companion object {
        const val TAG = "IncidentStore"
        const val PREFS_NAME = "urbanguard_incidents"
        const val KEY_PREFIX = "incidents_"
    }
}
