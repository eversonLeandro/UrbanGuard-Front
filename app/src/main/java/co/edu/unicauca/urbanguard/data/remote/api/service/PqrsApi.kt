package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.PqrsDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PqrsSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdatePqrsStatusRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface PqrsApi {
    @GET("api/complexes/{complexId}/pqrs")
    suspend fun getPqrs(
        @Path("complexId") complexId: String,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): List<PqrsSummaryDto>

    @GET("api/complexes/{complexId}/pqrs/{pqrsId}/admin")
    suspend fun getPqrsDetail(
        @Path("complexId") complexId: String,
        @Path("pqrsId") pqrsId: String
    ): PqrsDetailDto

    @PATCH("api/complexes/{complexId}/pqrs/{pqrsId}/status")
    suspend fun updatePqrsStatus(
        @Path("complexId") complexId: String,
        @Path("pqrsId") pqrsId: String,
        @Body body: UpdatePqrsStatusRequestDto
    ): PqrsDetailDto
}

