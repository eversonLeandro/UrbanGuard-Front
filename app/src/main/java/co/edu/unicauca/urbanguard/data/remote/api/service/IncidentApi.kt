package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentImageAccessDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface IncidentApi {

    @GET("api/complexes/{complexId}/patrol-reports/mine")
    suspend fun getMyIncidents(
        @Path("complexId") complexId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): List<IncidentResponseDto>

    @GET("api/complexes/{complexId}/patrol-reports")
    suspend fun getAllIncidents(
        @Path("complexId") complexId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): List<IncidentResponseDto>

    @GET("api/complexes/{complexId}/patrol-reports/{reportId}")
    suspend fun getIncident(
        @Path("complexId") complexId: String,
        @Path("reportId") reportId: String
    ): IncidentResponseDto

    @GET("api/complexes/{complexId}/patrol-reports/{reportId}/image-access")
    suspend fun getImageAccess(
        @Path("complexId") complexId: String,
        @Path("reportId") reportId: String
    ): IncidentImageAccessDto

    @Multipart
    @POST("api/complexes/{complexId}/patrol-reports")
    suspend fun reportIncident(
        @Path("complexId") complexId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Part("metadata") metadata: RequestBody,
        @Part image: MultipartBody.Part
    ): IncidentResponseDto
}
