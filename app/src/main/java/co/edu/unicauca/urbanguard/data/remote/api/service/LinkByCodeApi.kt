package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoSearchResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LinkByCodeApi {
    // Busca el conjunto por código de vinculación
    @GET("onboarding/search")
    suspend fun searchConjunto(
        @Query("code") code: String
    ): ConjuntoSearchResponseDto

    // Envía la solicitud de vinculación al conjunto
    @POST("onboarding/link/{conjuntoId}")
    suspend fun requestLink(
        @Path("conjuntoId") conjuntoId: String
    ): LinkRequestResponseDto
}