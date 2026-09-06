package co.edu.unicauca.urbanguard.data.remote.api.service
import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoCodeResponseDto
import retrofit2.http.POST
import retrofit2.http.GET

interface OnboardingApi {
    // Obtiene el código actual del conjunto (si ya fue generado)
    @GET("onboarding/code")
    suspend fun getCode(): ConjuntoCodeResponseDto

    // Primera generación del código
    @POST("onboarding/code/generate")
    suspend fun generateCode(): ConjuntoCodeResponseDto

    // Regenera el código invalidando el anterior
    @POST("onboarding/code/regenerate")
    suspend fun regenerateCode(): ConjuntoCodeResponseDto
}