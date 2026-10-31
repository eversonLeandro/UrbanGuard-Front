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
 * El `userId` y el rol los resuelve el backend del JWT: el cliente nunca los
 * envía. `conjuntoId` sí es obligatorio porque un usuario puede pertenecer a
 * varios conjuntos y la bandeja nunca debe mezclar avisos entre ellos. Las
 * mutaciones responden 204 sin cuerpo, por eso devuelven Unit.
 */
interface NotificationInboxApi {

    /**
     * GET api/notifications?conjuntoId=&page=&size=&unread=
     * Éxito: 200 con { items, page, size, totalElements, totalPages }.
     * `unread=true` filtra solo las no leídas; omitido devuelve todas.
     */
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("conjuntoId") conjuntoId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("unread") unread: Boolean?
    ): NotificationPageDto

    /** GET api/notifications/unread-count?conjuntoId= → { unreadCount } */
    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(@Query("conjuntoId") conjuntoId: String): UnreadCountDto

    /** PATCH api/notifications/{id}/leida?conjuntoId= → 204. */
    @PATCH("api/notifications/{id}/leida")
    suspend fun markRead(
        @Path("id") id: String,
        @Query("conjuntoId") conjuntoId: String
    )

    /** PATCH api/notifications/leidas?conjuntoId= → 204 (marca todas). */
    @PATCH("api/notifications/leidas")
    suspend fun markAllRead(@Query("conjuntoId") conjuntoId: String)
}
