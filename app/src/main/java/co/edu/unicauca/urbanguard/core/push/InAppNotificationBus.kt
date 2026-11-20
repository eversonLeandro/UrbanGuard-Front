package co.edu.unicauca.urbanguard.core.push

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Evento de notificación flotante in-app (push recibido en primer plano o alerta local). */
data class InAppNotificationEvent(
    val title: String,
    val body: String,
    val channel: String,
    val deepLink: String? = null
)

/**
 * Bus singleton que transporta eventos de notificación hacia el banner flotante
 * montado en la UI principal sin acoplar servicios Android con Compose.
 */
@Singleton
class InAppNotificationBus @Inject constructor() {

    private val _events = MutableSharedFlow<InAppNotificationEvent>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<InAppNotificationEvent> = _events.asSharedFlow()

    fun emit(event: InAppNotificationEvent) {
        _events.tryEmit(event)
    }
}
