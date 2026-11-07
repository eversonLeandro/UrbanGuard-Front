package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.DeliverPackageRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterPackageRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PackageApi
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.model.PackageMetrics
import co.edu.unicauca.urbanguard.domain.model.PackageRegistration
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
    override suspend fun metrics(complexId: String, from: String?, to: String?): Result<PackageMetrics> = safeCall {
        val dto = api.metrics(complexId, from, to)
        PackageMetrics(
            receivedCount = dto.receivedCount,
            deliveredCount = dto.deliveredCount,
            pendingCount = dto.pendingCount,
            returnedCount = dto.returnedCount,
            canceledCount = dto.canceledCount,
            averageDeliveryHours = dto.averageDeliveryHours,
            byStatus = dto.byStatus.associate { it.status to it.count },
            trend = dto.trend.map {
                co.edu.unicauca.urbanguard.domain.model.PackageTrendMetric(
                    it.date,
                    it.receivedCount,
                    it.deliveredCount
                )
            }
        )
    }

    override suspend fun list(complexId: String, status: String?): Result<List<Package>> = safeCall {
        api.list(complexId, status).map { it.toDomain() }
    }

    override suspend fun detail(complexId: String, packageId: String): Result<Package> = safeCall {
        api.detail(complexId, packageId).toDomain()
    }

    override suspend fun register(complexId: String, registration: PackageRegistration): Result<Package> = safeCall {
        val metadata = Gson().toJson(RegisterPackageRequestDto(
            registration.residentialUnitId,
            registration.residentialUnitLabel,
            registration.residentId,
            registration.recipientName,
            registration.sender,
            registration.description,
            registration.observations,
            null
        ))
        val metadataBody = metadata.toRequestBody("application/json; charset=utf-8".toMediaType())
        val imagePart = registration.photoPath?.let { path ->
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
