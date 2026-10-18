package co.edu.unicauca.urbanguard.ui.features.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado compartido del conteo de notificaciones no leídas.
 *
 * La campana aparece en varias pantallas; un único store singleton mantiene el
 * contador consistente entre todas y evita repetir la consulta por cada una.
 */
@Singleton
class NotificationBadgeStore @Inject constructor() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun set(count: Int) {
        _unreadCount.value = count.coerceAtLeast(0)
    }

    fun decrement() {
        set(_unreadCount.value - 1)
    }
}
