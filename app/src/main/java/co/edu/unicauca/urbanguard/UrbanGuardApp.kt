package co.edu.unicauca.urbanguard

import android.app.Application
import co.edu.unicauca.urbanguard.core.push.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UrbanGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Los canales deben existir antes de mostrar cualquier notificación
        // (obligatorio en Android 8+).
        NotificationChannels.createAll(this)
    }
}
