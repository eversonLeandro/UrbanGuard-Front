package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialUnitDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ResidentialApi {
    @GET("api/complexes/{complexId}/census")
    suspend fun getCensus(
        @Path("complexId") complexId: String
    ): List<ResidentialUnitDto>
}
