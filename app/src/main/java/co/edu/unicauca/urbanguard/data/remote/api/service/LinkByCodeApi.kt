package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoSearchResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateLinkRequestRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LinkByCodeApi {
    // Busca el conjunto por código de vinculación
    @GET("api/community/conjuntos/codigo/{codigo}")
    suspend fun searchConjunto(
        @Path("codigo") code: String
    ): ConjuntoSearchResponseDto

    // Envía la solicitud de vinculación al conjunto, indicando el número de
    // apartamento/unidad del solicitante
    @POST("api/community/solicitudes-vinculacion")
    suspend fun requestLink(
        @Body body: CreateLinkRequestRequestDto
    ): LinkRequestResponseDto
}
