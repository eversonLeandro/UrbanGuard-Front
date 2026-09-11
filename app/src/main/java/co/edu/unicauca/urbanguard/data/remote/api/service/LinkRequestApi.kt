package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ReviewLinkRequestRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Endpoints de administración de solicitudes de vinculación (UG-49).
interface LinkRequestApi {
    // Lista todas las solicitudes de vinculación del conjunto.
    @GET("onboarding/link-requests")
    suspend fun getLinkRequests(): List<LinkRequestDto>

    // Detalle de una solicitud por su ID.
    @GET("onboarding/link-requests/{id}")
    suspend fun getLinkRequestById(
        @Path("id") id: String
    ): LinkRequestDto

    // Aprueba (approve=true) o rechaza (approve=false) una solicitud.
    @POST("onboarding/link-requests/{id}/review")
    suspend fun reviewLinkRequest(
        @Path("id") id: String,
        @Body request: ReviewLinkRequestRequestDto
    ): LinkRequestDto
}