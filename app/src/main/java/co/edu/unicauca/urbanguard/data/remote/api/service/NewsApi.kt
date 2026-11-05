package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.AnnouncementImageAccessDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface NewsApi {

    @GET("api/complexes/{complexId}/announcements")
    suspend fun getAnnouncements(
        @Path("complexId") complexId: String
    ): List<NewsDto>

    @Multipart
    @POST("api/complexes/{complexId}/announcements")
    suspend fun publishAnnouncement(
        @Path("complexId") complexId: String,
        @Part("metadata") metadata: RequestBody,
        @Part attachment: MultipartBody.Part? = null
    ): NewsDto

    @Multipart
    @PUT("api/complexes/{complexId}/announcements/{announcementId}")
    suspend fun updateAnnouncement(
        @Path("complexId") complexId: String,
        @Path("announcementId") announcementId: String,
        @Part("metadata") metadata: RequestBody,
        @Part attachment: MultipartBody.Part? = null,
        @Query("removeAttachment") removeAttachment: Boolean = false
    ): NewsDto

    @DELETE("api/complexes/{complexId}/announcements/{announcementId}")
    suspend fun deleteAnnouncement(
        @Path("complexId") complexId: String,
        @Path("announcementId") announcementId: String
    )

    @GET("api/complexes/{complexId}/announcements/{announcementId}/image-access")
    suspend fun getAnnouncementImageUrl(
        @Path("complexId") complexId: String,
        @Path("announcementId") announcementId: String
    ): AnnouncementImageAccessDto

    @Streaming
    @GET("api/complexes/{complexId}/announcements/{announcementId}/attachment")
    suspend fun downloadAttachment(
        @Path("complexId") complexId: String,
        @Path("announcementId") announcementId: String
    ): ResponseBody
}
