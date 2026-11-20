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
    const val PQRS = "pqrs"
    const val PACKAGES = "packages"

    private val SUPPORTED_CHANNELS = setOf(
        VISITORS,
        REQUESTS,
        ANNOUNCEMENTS,
        PATROL,
        PQRS,
        PACKAGES
    )

    /**
     * Resuelve el canal priorizando el `channel` explícito enviado por el
     * backend y haciendo fallback al prefijo del `deepLink`.
     */
    fun resolveChannel(explicitChannel: String?, deepLink: String?): String {
        val normalized = explicitChannel?.trim()?.lowercase()
        if (normalized != null && normalized in SUPPORTED_CHANNELS) {
            return normalized
        }
        return fromDeepLink(deepLink)
    }

    /**
     * Deriva el canal desde el deepLink del backend
     * (`urbanguard://visitors/{id}`). Prefijo desconocido o ausente → avisos.
     */
    fun fromDeepLink(deepLink: String?): String = when {
        deepLink == null -> ANNOUNCEMENTS
        deepLink.startsWith("urbanguard://visitors") -> VISITORS
        deepLink.startsWith("urbanguard://requests") -> REQUESTS
        deepLink.startsWith("urbanguard://patrol-reports") -> PATROL
        deepLink.startsWith("urbanguard://patrol") -> PATROL
        deepLink.startsWith("urbanguard://packages") -> PACKAGES
        deepLink.startsWith("urbanguard://community") -> ANNOUNCEMENTS
        deepLink.startsWith("urbanguard://news") -> ANNOUNCEMENTS
        deepLink.startsWith("urbanguard://pqrs") -> PQRS
        else -> ANNOUNCEMENTS
    }

    /**
     * Crea los canales con importancia alta (`IMPORTANCE_HIGH`) en los eventos
     * accionables para que Android muestre notificación flotante (heads-up).
     */
    fun createAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            channel(VISITORS, "Visitantes", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            channel(REQUESTS, "Solicitudes", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            channel(ANNOUNCEMENTS, "Avisos", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            channel(PATROL, "Rondas y Seguridad", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            channel(PQRS, "PQRS", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            channel(PACKAGES, "Paquetería", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun channel(id: String, name: String, importance: Int) =
        NotificationChannel(id, name, importance)
}
