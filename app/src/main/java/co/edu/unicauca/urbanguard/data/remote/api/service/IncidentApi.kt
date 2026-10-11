package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.core.common.IncidentConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Novedades de vigilancia (UG-43 / UG-99).
 *
 * Contrato propuesto para el backend: creación `multipart` (campos + foto)
 * y listado. Mientras [IncidentConstants.INCIDENTS_BACKEND_ENABLED] esté
 * apagado el repositorio no invoca estos métodos.
 */
interface IncidentApi {

    @GET("api/complexes/{complexId}/incidents")
    suspend fun getIncidents(
        @Path("complexId") complexId: String
    ): List<IncidentResponseDto>

    @Multipart
    @POST("api/complexes/{complexId}/incidents")
    suspend fun reportIncident(
        @Path("complexId") complexId: String,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody,
        @Part pointId: MultipartBody.Part? = null,
        @Part photo: MultipartBody.Part? = null
    ): IncidentResponseDto
}
