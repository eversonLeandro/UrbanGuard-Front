package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.LoginRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.LoginResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PasswordResetRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PasswordResetResponseDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/logout")
    suspend fun logout()

    @Headers("X-UrbanGuard-Public-Request: password-reset")
    @POST("api/auth/password-reset")
    suspend fun requestPasswordReset(
        @Body request: PasswordResetRequestDto
    ): PasswordResetResponseDto
}
