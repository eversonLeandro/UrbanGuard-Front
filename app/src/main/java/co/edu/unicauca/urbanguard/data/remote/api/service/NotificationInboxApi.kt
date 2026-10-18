package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.NotificationPageDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UnreadCountDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Bandeja de notificaciones in-app (notification-service vía API Gateway).
 *
 * El `userId` lo resuelve el backend del JWT: el cliente nunca lo envía. Las
 * mutaciones responden 204 sin cuerpo, por eso devuelven Unit.
 */
interface NotificationInboxApi {

    /**
     * GET api/notifications?page=&size=&unread=
     * Éxito: 200 con { items, page, size, totalElements, totalPages }.
     * `unread=true` filtra solo las no leídas; omitido devuelve todas.
     */
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("unread") unread: Boolean?
    ): NotificationPageDto

    /** GET api/notifications/unread-count → { unreadCount } */
    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountDto

    /** PATCH api/notifications/{id}/leida → 204. */
    @PATCH("api/notifications/{id}/leida")
    suspend fun markRead(@Path("id") id: String)

    /** PATCH api/notifications/leidas → 204 (marca todas). */
    @PATCH("api/notifications/leidas")
    suspend fun markAllRead()
}
