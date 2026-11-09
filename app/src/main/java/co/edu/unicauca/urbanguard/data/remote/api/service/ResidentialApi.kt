package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateOwnerTenantRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateUnitResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.OwnerTenantResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialUnitDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdateUnitRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdateUnitResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

interface ResidentialApi {
    @GET("api/complexes/{complexId}/census")
    suspend fun getCensus(
        @Path("complexId") complexId: String,
        @Query("estado") estado: String = "activos"
    ): List<ResidentialUnitDto>

    @POST("api/complexes/{complexId}/units")
    suspend fun createUnit(
        @Path("complexId") complexId: String
    ): CreateUnitResponseDto

    @PATCH("api/complexes/{complexId}/units/{unitId}")
    suspend fun updateUnit(
        @Path("complexId") complexId: String,
        @Path("unitId") unitId: String,
        @Body request: UpdateUnitRequestDto
    ): UpdateUnitResponseDto

    @POST("api/complexes/{complexId}/units/{unitId}/residents")
    suspend fun createOwnerTenant(
        @Path("complexId") complexId: String,
        @Path("unitId") unitId: String,
        @Body request: CreateOwnerTenantRequestDto
    ): OwnerTenantResponseDto

    @DELETE("api/complexes/{complexId}/units/{unitId}/residents/{residentId}")
    suspend fun deactivateOwnerTenant(
        @Path("complexId") complexId: String,
        @Path("unitId") unitId: String,
        @Path("residentId") residentId: String
    )

    @POST("api/complexes/{complexId}/units/{unitId}/residents/{residentId}/reactivate")
    suspend fun reactivateOwnerTenant(
        @Path("complexId") complexId: String,
        @Path("unitId") unitId: String,
        @Path("residentId") residentId: String
    )
}
