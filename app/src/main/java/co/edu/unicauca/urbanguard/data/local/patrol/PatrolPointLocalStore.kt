package co.edu.unicauca.urbanguard.data.local.patrol

import android.content.Context
import android.util.Log
import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointResponseDto
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caché local de checkpoints por conjunto.
 *
 * La clave cambió a `points_v2_` porque el contrato anterior incluía `id`,
 * `order` y `nfcTagId` planos. Los datos viejos se leen desde la clave
 * heredada únicamente para migrar rutas y rondas.
 */
@Singleton
class PatrolPointLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val storage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(context, PREFS_NAME)
    }
    private val gson = Gson()

    /** Checkpoints actuales del conjunto. */
    @Suppress("TooGenericExceptionCaught")
    fun load(complexId: String): List<CheckpointResponseDto> {
        return try {
            val raw = storage.retrieveString(keyFor(complexId)) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            val type = object : TypeToken<List<CheckpointResponseDto>>() {}.type
            gson.fromJson<List<CheckpointResponseDto>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer los checkpoints", e)
            emptyList()
        }
    }

    /** Reemplaza la caché del conjunto. */
    @Suppress("TooGenericExceptionCaught")
    fun save(complexId: String, points: List<CheckpointResponseDto>) {
        try {
            storage.store(keyFor(complexId), gson.toJson(points))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron guardar los checkpoints", e)
        }
    }

    /** Puntos del formato anterior; se conservan solo hasta completar la migración. */
    @Suppress("TooGenericExceptionCaught")
    fun loadLegacy(complexId: String): List<PatrolLegacyPointDto> {
        return try {
            val raw = storage.retrieveString(legacyKeyFor(complexId)) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            val type = object : TypeToken<List<PatrolLegacyPointDto>>() {}.type
            gson.fromJson<List<PatrolLegacyPointDto>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer los puntos heredados", e)
            emptyList()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun clearLegacy(complexId: String) {
        try {
            storage.store(legacyKeyFor(complexId), "")
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo limpiar la caché heredada", e)
        }
    }

    private fun keyFor(complexId: String): String = "$KEY_PREFIX$complexId"

    private fun legacyKeyFor(complexId: String): String = "$LEGACY_KEY_PREFIX$complexId"

    private companion object {
        const val TAG = "PatrolStore"
        const val PREFS_NAME = "urbanguard_patrol_points"
        const val KEY_PREFIX = "points_v2_"
        const val LEGACY_KEY_PREFIX = "points_"
    }
}
