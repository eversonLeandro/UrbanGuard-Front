package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RejectLinkRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de administración de solicitudes de vinculación (UG-49).
interface LinkRequestApi {
    // Lista todas las solicitudes de vinculación del conjunto.
    @GET("api/community/solicitudes-vinculacion")
    suspend fun getLinkRequests(@Query("estado") status: String = "PENDING"): List<LinkRequestDto>

    // Aprueba (approve=true) o rechaza (approve=false) una solicitud.
    @PATCH("api/community/solicitudes-vinculacion/{id}/aprobar")
    suspend fun approveLinkRequest(@Path("id") id: String): LinkRequestDto

    @PATCH("api/community/solicitudes-vinculacion/{id}/rechazar")
    suspend fun rejectLinkRequest(
        @Path("id") id: String,
        @Body request: RejectLinkRequestDto = RejectLinkRequestDto()
    )
}