package co.edu.unicauca.urbanguard.core.push

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import co.edu.unicauca.urbanguard.MainActivity
import co.edu.unicauca.urbanguard.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

/**
 * Recepción de push (FCM). El sistema lo invoca incluso con la app cerrada.
 *
 * - [onNewToken]: persiste el token y lo registra si ya hay conjunto activo.
 * - [onMessageReceived]: con la app en primer plano FCM no muestra nada solo,
 *   así que se publica la notificación del sistema a mano. En segundo plano o
 *   cerrada el sistema la muestra directo con el `notification` del backend y
 *   el tap abre [MainActivity] vía el PendingIntent aquí construido.
 */
@AndroidEntryPoint
class UrbanGuardMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushRegistrar: PushRegistrar

    override fun onNewToken(token: String) {
        Log.d(TAG, "Token FCM rotado, re-registrando")
        pushRegistrar.onTokenRotated(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val deepLink = data["deepLink"]
        val channelId = NotificationChannels.fromDeepLink(deepLink)
        val title = message.notification?.title ?: "UrbanGuard"
        val body = message.notification?.body ?: return

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
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
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
