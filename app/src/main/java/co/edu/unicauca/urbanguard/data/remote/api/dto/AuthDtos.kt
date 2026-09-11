package co.edu.unicauca.urbanguard.data.remote.api.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val token: String,
    val userId: String,
    val name: String,
    val role: String,
    val hasConjunto: Boolean = false
)
