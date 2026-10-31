package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePqrsRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsCreatedDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsSummaryDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PqrsResidentApi {
    @POST("api/complexes/{complexId}/pqrs")
    suspend fun create(
        @Path("complexId") complexId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreatePqrsRequestDto
    ): ResidentPqrsCreatedDto

    @GET("api/complexes/{complexId}/pqrs/mine")
    suspend fun getMine(
        @Path("complexId") complexId: String,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): List<ResidentPqrsSummaryDto>

    @GET("api/complexes/{complexId}/pqrs/{pqrsId}")
    suspend fun getOwnDetail(
        @Path("complexId") complexId: String,
        @Path("pqrsId") pqrsId: String
    ): ResidentPqrsDetailDto
}
