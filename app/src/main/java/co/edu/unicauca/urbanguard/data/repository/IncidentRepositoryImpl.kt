package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.IncidentConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.incident.IncidentLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.service.IncidentApi
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.IncidentSyncState
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class IncidentRepositoryImpl @Inject constructor(
    private val api: IncidentApi,
    private val localStore: IncidentLocalStore
) : IncidentRepository {

    private var selected: SecurityIncident? = null

    override suspend fun getIncidents(complexId: String): Result<List<SecurityIncident>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(IncidentConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (!IncidentConstants.INCIDENTS_BACKEND_ENABLED) {
            return Result.success(localIncidents(complexId))
        }
        return try {
            val remote = api.getIncidents(complexId)
            mergeRemote(complexId, remote)
            Result.success(localIncidents(complexId))
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar novedades; se usa caché local", e)
            Result.success(localIncidents(complexId))
        }
    }

    override suspend fun reportIncident(
        complexId: String,
        category: IncidentCategory,
        description: String,
        photoPath: String?,
        pointId: String?,
        pointName: String?
    ): Result<SecurityIncident> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(IncidentConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        var incident = SecurityIncident(
            id = UUID.randomUUID().toString(),
            complexId = complexId,
            category = category,
            description = description.trim(),
            photoPath = photoPath,
            pointId = pointId,
            pointName = pointName,
            createdAt = Instant.now(),
            syncState = IncidentSyncState.PENDING
        )
        upsertLocal(incident)
        if (IncidentConstants.INCIDENTS_BACKEND_ENABLED) {
            uploadBestEffort(incident)?.let { incident = it }
        }
        return Result.success(incident)
    }

    override suspend fun retryPending(complexId: String): Result<Unit> {
        if (!IncidentConstants.INCIDENTS_BACKEND_ENABLED) return Result.success(Unit)
        localIncidents(complexId)
            .filter { it.syncState == IncidentSyncState.PENDING }
            .forEach { uploadBestEffort(it) }
        return Result.success(Unit)
    }

    override fun setSelectedIncident(incident: SecurityIncident?) {
        selected = incident
    }

    override fun selectedIncident(): SecurityIncident? = selected

    private fun localIncidents(complexId: String): List<SecurityIncident> =
        localStore.load(complexId)
            .map { it.toDomain(complexId) }
            .sortedByDescending { it.createdAt }

    private fun upsertLocal(incident: SecurityIncident) {
        val current = localIncidents(incident.complexId).toMutableList()
        val index = current.indexOfFirst { it.id == incident.id }
        if (index == -1) current.add(0, incident) else current[index] = incident
        persistAll(incident.complexId, current)
    }

    private fun persistAll(complexId: String, incidents: List<SecurityIncident>) {
        localStore.save(complexId, incidents.map { it.toResponseDto() })
    }

    /**
     * Fusiona el listado remoto conservando la foto local: el backend sirve
     * URL y el equipo ya tiene el archivo.
     */
    private fun mergeRemote(
        complexId: String,
        remote: List<IncidentResponseDto>
    ) {
        val localPhotos = localIncidents(complexId)
            .associate { it.id to it.photoPath }
        val merged = remote.map { dto ->
            val photo = localPhotos[dto.id] ?: dto.photoUrl
            dto.toDomain(complexId, localPhotoPath = photo)
        }
        persistAll(complexId, merged.sortedByDescending { it.createdAt })
    }

    private suspend fun uploadBestEffort(incident: SecurityIncident): SecurityIncident? {
        return try {
            val photoPart = incident.photoPath
                ?.takeIf { File(it).exists() }
                ?.let { path ->
                    val file = File(path)
                    MultipartBody.Part.createFormData(
                        "photo",
                        file.name,
                        file.asRequestBody("image/jpeg".toMediaType())
                    )
                }
            val pointPart = incident.pointId?.let { pointId ->
                MultipartBody.Part.createFormData(
                    "pointId",
                    pointId,
                    pointId.toRequestBody("text/plain".toMediaType())
                )
            }
            val response = api.reportIncident(
                complexId = incident.complexId,
                category = incident.category.name.toRequestBody("text/plain".toMediaType()),
                description = incident.description.toRequestBody("text/plain".toMediaType()),
                pointId = pointPart,
                photo = photoPart
            )
            val synced = response.toDomain(
                complexId = incident.complexId,
                localPhotoPath = incident.photoPath
            )
            upsertLocal(synced)
            synced
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo enviar la novedad; queda pendiente", e)
            null
        } catch (e: HttpException) {
            Log.w(TAG, "El backend rechazó la novedad: ${e.code()}")
            null
        }
    }

    private companion object {
        const val TAG = "IncidentRepo"
    }
}
