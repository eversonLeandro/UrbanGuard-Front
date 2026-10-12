package co.edu.unicauca.urbanguard.core.push

import android.content.Context
import android.provider.Settings
import android.util.Log
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.notifications.RegisterDeviceTokenUseCase
import co.edu.unicauca.urbanguard.domain.usecase.notifications.UnregisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
// Cinco colaboradores inyectados (contexto, sesión, store y dos casos de uso):
// partir la clase perdería el único punto de coordinación del registro push.
@Suppress("LongParameterList")
class PushRegistrarImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val tokenStore: FcmTokenStore,
    private val registerDeviceToken: RegisterDeviceTokenUseCase,
    private val unregisterDeviceToken: UnregisterDeviceTokenUseCase
) : PushRegistrar {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun registerActiveConjunto() {
        val conjuntoId = sessionManager.getActiveConjunto()?.conjuntoId
        if (conjuntoId == null) {
            Log.d(TAG, "Sin conjunto activo, se omite el registro push")
            return
        }
        register(conjuntoId)
    }

    override fun registerForConjunto(conjuntoId: String) {
        register(conjuntoId)
    }

    private fun register(conjuntoId: String) {
        scope.launch {
            val fcmToken = fetchFcmToken() ?: return@launch
            tokenStore.token = fcmToken
            registerDeviceToken(fcmToken, deviceId(), conjuntoId)
                .onFailure { Log.w(TAG, "No se pudo registrar el token push: ${it.message}") }
        }
    }

    override fun onTokenRotated(token: String) {
        tokenStore.token = token
        registerActiveConjunto()
    }

    override fun unregisterCurrent(onComplete: () -> Unit) {
        scope.launch {
            try {
                val fcmToken = tokenStore.token ?: fetchFcmToken()
                if (fcmToken != null) {
                    unregisterDeviceToken(fcmToken)
                        .onFailure { Log.w(TAG, "No se pudo desregistrar el token push: ${it.message}") }
                }
            } finally {
                tokenStore.clear()
                onComplete()
            }
        }
    }

    // Frontera best-effort: las Tasks de Firebase fallan con excepciones
    // variadas (ApiException, IllegalStateException...) y ninguna debe romper
    // el flujo que llama.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchFcmToken(): String? = try {
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo obtener el token FCM: ${e.message}")
        null
    }

    @Suppress("TooGenericExceptionCaught")
    private fun deviceId(): String? = try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo leer ANDROID_ID: ${e.message}")
        null
    }

    private companion object {
        const val TAG = "PushRegistrar"
    }
}
