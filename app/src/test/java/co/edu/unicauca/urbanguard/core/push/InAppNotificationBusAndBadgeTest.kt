package co.edu.unicauca.urbanguard.core.push

import co.edu.unicauca.urbanguard.ui.features.notifications.NotificationBadgeStore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InAppNotificationBusAndBadgeTest {

    @Test
    fun `badge store incrementa decrementa y no baja de cero`() {
        val store = NotificationBadgeStore()

        assertEquals(0, store.unreadCount.value)
        store.increment()
        store.increment()
        assertEquals(2, store.unreadCount.value)
        store.decrement()
        assertEquals(1, store.unreadCount.value)
        store.decrement()
        store.decrement()
        assertEquals(0, store.unreadCount.value)
        store.set(-5)
        assertEquals(0, store.unreadCount.value)
    }

    @Test
    fun `inAppNotificationBus entrega eventos emitidos a suscriptores`() = runTest {
        val bus = InAppNotificationBus()
        val expected = InAppNotificationEvent(
            title = "Nueva solicitud de vinculación",
            body = "Unidad Torre A - 101",
            channel = NotificationChannels.REQUESTS,
            deepLink = "urbanguard://requests/admin"
        )

        val deferred = async { bus.events.first() }
        testScheduler.runCurrent()
        bus.emit(expected)

        assertEquals(expected, deferred.await())
    }
}
