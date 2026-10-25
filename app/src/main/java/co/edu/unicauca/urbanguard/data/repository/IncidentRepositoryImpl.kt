package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateIncidentRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.IncidentApi
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class IncidentRepositoryImpl @Inject constructor(
    private val api: IncidentApi
) : IncidentRepository {

    override suspend fun getIncidents(complexId: String, isAdmin: Boolean): Result<List<SecurityIncident>> {
        if (complexId.isBlank()) return Result.failure(IllegalArgumentException("No hay conjunto activo."))
        return try {
            val today = LocalDate.now(ZoneId.of("America/Bogota"))
            val from = today.minusDays(30).toString()
            val to = today.toString()
            val response = if (isAdmin) {
                api.getAllIncidents(complexId, from, to)
            } else {
                api.getMyIncidents(complexId, from, to)
            }
            Result.success(response.map { it.toDomain() })
        } catch (error: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(error), error))
        } catch (error: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor. Revisa tu conexión.", error))
        }
    }

    override suspend fun getIncident(complexId: String, reportId: String): Result<SecurityIncident> {
        if (complexId.isBlank()) return Result.failure(IllegalArgumentException("No hay conjunto activo."))
        return try {
            val report = api.getIncident(complexId, reportId)
            val imageUrl = api.getImageAccess(complexId, reportId).url
            Result.success(report.toDomain(imageUrl = imageUrl))
        } catch (error: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(error), error))
        } catch (error: IOException) {
            Result.failure(Exception("No se pudo cargar la novedad. Revisa tu conexión.", error))
        }
    }

    override suspend fun reportIncident(
        complexId: String,
        category: IncidentCategory,
        description: String,
        photoPath: String?,
        pointId: String?,
        executionId: String?,
        idempotencyKey: String
    ): Result<SecurityIncident> {
        if (complexId.isBlank()) return Result.failure(IllegalArgumentException("No hay conjunto activo."))
        if (photoPath.isNullOrBlank() || !File(photoPath).isFile) {
            return Result.failure(IllegalArgumentException("Debes tomar una foto para reportar la novedad."))
        }
        return try {
            val metadata = CreateIncidentRequestDto(
                description = description.trim(),
                category = category.name,
                executionId = executionId,
                checkpointId = pointId
            )
            val metadataBody = Gson().toJson(metadata)
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            val imageFile = File(photoPath)
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/jpeg".toMediaType())
            )
            val response = api.reportIncident(complexId, idempotencyKey, metadataBody, imagePart)
            Result.success(response.toDomain())
        } catch (error: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(error), error))
        } catch (error: IOException) {
            Result.failure(Exception("No se pudo enviar la novedad. Revisa tu conexión.", error))
        }
    }
}
