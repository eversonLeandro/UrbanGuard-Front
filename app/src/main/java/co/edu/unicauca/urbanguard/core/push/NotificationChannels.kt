package co.edu.unicauca.urbanguard.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat

/**
 * Canales de notificación de la app. Los IDs deben coincidir exacto con el
 * `channel` que envía el backend (`notification-service`, contrato de
 * notifications.queue): si el canal no existe, Android 8+ silencia el push.
 */
object NotificationChannels {
    const val VISITORS = "visitors"
    const val REQUESTS = "requests"
    const val ANNOUNCEMENTS = "announcements"
    const val PATROL = "patrol"

    /**
     * Deriva el canal desde el deepLink del backend
     * (`urbanguard://visitors/{id}`). Prefijo desconocido o ausente → avisos.
     */
    fun fromDeepLink(deepLink: String?): String = when {
        deepLink == null -> ANNOUNCEMENTS
        deepLink.startsWith("urbanguard://visitors") -> VISITORS
        deepLink.startsWith("urbanguard://requests") -> REQUESTS
        deepLink.startsWith("urbanguard://patrol") -> PATROL
        deepLink.startsWith("urbanguard://community") -> ANNOUNCEMENTS
        else -> ANNOUNCEMENTS
    }

    /**
     * Crea los canales. El ID de [PATROL] debe ser exactamente `"patrol"` para
     * coincidir con el `channel` que envía el backend, así que no se puede
     * renombrar para forzar la nueva importancia: Android ignora los cambios de
     * importancia en un canal ya creado. Para ver `IMPORTANCE_HIGH` en un
     * dispositivo donde el canal ya existía hay que reinstalar la app (o
     * borrar sus datos); en instalaciones nuevas aplica directo.
     */
    fun createAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            channel(VISITORS, "Visitantes", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            channel(REQUESTS, "Solicitudes", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            channel(ANNOUNCEMENTS, "Avisos", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            channel(PATROL, "Rondas", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun channel(id: String, name: String, importance: Int) =
        NotificationChannel(id, name, importance)
}
