package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.DeliverPackageRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterPackageRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PackageApi
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException
import com.google.gson.Gson
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class PackageRepositoryImpl @Inject constructor(private val api: PackageApi) : PackageRepository {
    override suspend fun list(complexId: String, status: String?): Result<List<Package>> = safeCall {
        api.list(complexId, status).map { it.toDomain() }
    }

    override suspend fun detail(complexId: String, packageId: String): Result<Package> = safeCall {
        api.detail(complexId, packageId).toDomain()
    }

    override suspend fun register(
        complexId: String,
        residentialUnitId: String?,
        residentialUnitLabel: String?,
        residentId: String?,
        recipientName: String,
        sender: String?,
        description: String,
        observations: String?,
        photoPath: String?
    ): Result<Package> = safeCall {
        val metadata = Gson().toJson(RegisterPackageRequestDto(
            residentialUnitId, residentialUnitLabel, residentId, recipientName, sender,
            description, observations, null
        ))
        val metadataBody = metadata.toRequestBody("application/json; charset=utf-8".toMediaType())
        val imagePart = photoPath?.let { path ->
            val file = File(path)
            if (file.isFile) MultipartBody.Part.createFormData(
                "image", file.name, file.asRequestBody("image/jpeg".toMediaType())
            ) else null
        }
        api.register(complexId, metadataBody, imagePart).toDomain()
    }

    override suspend fun deliver(
        complexId: String,
        packageId: String,
        deliveredTo: String
    ): Result<Package> = safeCall {
        api.deliver(complexId, packageId, DeliverPackageRequestDto(deliveredTo)).toDomain()
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: HttpException) {
        Result.failure(Exception(NetworkErrorMapper.map(error), error))
    } catch (error: IOException) {
        Result.failure(Exception(NetworkErrorMapper.map(error), error))
    }
}
