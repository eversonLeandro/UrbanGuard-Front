package co.edu.unicauca.urbanguard.data.remote.api.service
import co.edu.unicauca.urbanguard.data.remote.api.dto.ActivationValidationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoCodeResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.OnboardingStatusDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterConjuntoRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OnboardingApi {
    // Consulta si el usuario Admin ya tiene un conjunto vinculado (UG-45)
    @GET("onboarding/status")
    suspend fun getStatus(): OnboardingStatusDto

    // Obtiene el código actual del conjunto (si ya fue generado)
    @GET("onboarding/code")
    suspend fun getCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // Primera generación del código para un conjunto específico
    @POST("onboarding/code/generate")
    suspend fun generateCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // Regenera el código invalidando el anterior
    @POST("onboarding/code/regenerate")
    suspend fun regenerateCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // =========================================================================
    // ENDPOINT NUEVO SOLICITADO AL BACKEND (NO EXISTE AÚN):
    //   POST onboarding/activation-code/validate
    //   Body:  { "codigoActivacion": "ACT-XXXX-YYYY" }
    //   Resp:  { "conjuntoId": "<PK autogenerada del conjunto>" }
    // El backend, al validar el código, crea el registro del Conjunto con PK
    // autogenerada y la retorna. NO retorna el código de activación de vuelta.
    // =========================================================================
    // DESCOMENTAR CUANDO ESTÉ EL BACKEND
    // @POST("onboarding/activation-code/validate")
    // suspend fun validateActivationCode(
    //     @Body request: ActivationValidationRequestDto
    // ): ActivationValidationResponseDto

    // Actualiza los datos de un conjunto YA existente por su ID.
    // El conjunto se crea en el paso de ActivateConjunto (validar código);
    // aquí solo se completan los datos (nombre, dirección, ciudad, unidades).
    // Ya NO se envía codigoActivacion: ese código murió en el paso de Activate.
    @PUT("onboarding/conjunto/{conjuntoId}")
    suspend fun updateConjunto(
        @Path("conjuntoId") conjuntoId: String,
        @Body request: RegisterConjuntoRequestDto
    ): Unit
}
