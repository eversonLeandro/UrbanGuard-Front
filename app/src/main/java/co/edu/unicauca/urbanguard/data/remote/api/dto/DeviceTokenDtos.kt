package co.edu.unicauca.urbanguard.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class RegisterDeviceTokenRequestDto(
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("deviceId") val deviceId: String?,
    @SerializedName("conjuntoId") val conjuntoId: String
)

data class DeregisterDeviceTokenRequestDto(
    @SerializedName("fcmToken") val fcmToken: String
)
