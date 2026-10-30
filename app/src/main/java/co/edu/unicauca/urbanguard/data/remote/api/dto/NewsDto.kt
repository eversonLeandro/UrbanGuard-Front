package co.edu.unicauca.urbanguard.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class NewsAttachmentDto(
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("contentType") val contentType: String? = null,
    @SerializedName("sizeBytes") val sizeBytes: Long? = null,
    @SerializedName("hasImage") val hasImage: Boolean? = null
)

data class AnnouncementImageAccessDto(
    @SerializedName("url") val url: String,
    @SerializedName("expiresAt") val expiresAt: String? = null
)

data class NewsDto(
    @SerializedName("announcementId") val announcementId: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("publishedAt") val publishedAt: String,
    @SerializedName("attachment") val attachment: NewsAttachmentDto? = null
)

data class NewsMetadataDto(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
