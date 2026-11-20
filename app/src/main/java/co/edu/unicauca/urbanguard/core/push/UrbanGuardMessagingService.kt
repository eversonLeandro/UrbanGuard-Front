package co.edu.unicauca.urbanguard.core.push

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.edu.unicauca.urbanguard.MainActivity
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.ui.features.notifications.NotificationBadgeStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

/**
 * Recepción de push (FCM). El sistema lo invoca incluso con la app cerrada.
 *
 * - [onNewToken]: persiste el token y lo registra si ya hay conjunto activo.
 * - [onMessageReceived]: con la app en primer plano actualiza el badge de la
 *   campana, emite el banner flotante in-app y publica la notificación del
 *   sistema con prioridad alta (heads-up).
 */
@AndroidEntryPoint
class UrbanGuardMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushRegistrar: PushRegistrar

    @Inject
    lateinit var notificationBadgeStore: NotificationBadgeStore

    @Inject
    lateinit var inAppNotificationBus: InAppNotificationBus

    override fun onNewToken(token: String) {
        Log.d(TAG, "Token FCM rotado, re-registrando")
        pushRegistrar.onTokenRotated(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val deepLink = data["deepLink"]
        val channelId = NotificationChannels.resolveChannel(
            explicitChannel = message.notification?.channelId ?: data["channel"],
            deepLink = deepLink
        )
        val title = message.notification?.title ?: data["title"] ?: "UrbanGuard"
        val body = message.notification?.body ?: data["body"] ?: return

        notificationBadgeStore.increment()
        inAppNotificationBus.emit(
            InAppNotificationEvent(
                title = title,
                body = body,
                channel = channelId,
                deepLink = deepLink
            )
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_DEEP_LINK, deepLink)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pending = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        try {
            NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Sin permiso POST_NOTIFICATIONS, se descarta el push", e)
        }
    }

    companion object {
        private const val TAG = "PushService"
        const val EXTRA_DEEP_LINK = "deep_link"
    }
}
