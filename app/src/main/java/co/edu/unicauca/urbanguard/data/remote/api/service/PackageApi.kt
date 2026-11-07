package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.DeliverPackageRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PackageDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PackageMetricsDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterPackageRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface PackageApi {
    @GET("api/complexes/{complexId}/packages/metrics")
    suspend fun metrics(
        @Path("complexId") complexId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): PackageMetricsDto

    @Multipart
    @POST("api/complexes/{complexId}/packages")
    suspend fun register(
        @Path("complexId") complexId: String,
        @Part("metadata") metadata: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): PackageDto

    @GET("api/complexes/{complexId}/packages")
    suspend fun list(@Path("complexId") complexId: String, @Query("status") status: String? = null): List<PackageDto>

    @GET("api/complexes/{complexId}/packages/{packageId}")
    suspend fun detail(
        @Path("complexId") complexId: String,
        @Path("packageId") packageId: String
    ): PackageDto

    @POST("api/complexes/{complexId}/packages/{packageId}/deliver")
    suspend fun deliver(
        @Path("complexId") complexId: String,
        @Path("packageId") packageId: String,
        @Body request: DeliverPackageRequestDto
    ): PackageDto
}
