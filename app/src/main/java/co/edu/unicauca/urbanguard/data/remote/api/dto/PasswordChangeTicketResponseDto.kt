package co.edu.unicauca.urbanguard.data.remote.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del backend con la URL temporal del ticket de cambio de contraseña de Auth0.
 */
data class PasswordChangeTicketResponseDto(
    @SerializedName("url")
    val url: String
)
