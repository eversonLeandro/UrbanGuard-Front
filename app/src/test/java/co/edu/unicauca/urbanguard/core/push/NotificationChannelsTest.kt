package co.edu.unicauca.urbanguard.core.push

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationChannelsTest {

    @Test
    fun `fromDeepLink mapea cada prefijo a su canal correspondiente`() {
        assertEquals(NotificationChannels.VISITORS, NotificationChannels.fromDeepLink("urbanguard://visitors/1"))
        assertEquals(NotificationChannels.REQUESTS, NotificationChannels.fromDeepLink("urbanguard://requests/admin"))
        assertEquals(NotificationChannels.PATROL, NotificationChannels.fromDeepLink("urbanguard://patrol-reports/1"))
        assertEquals(NotificationChannels.PATROL, NotificationChannels.fromDeepLink("urbanguard://patrol/guard"))
        assertEquals(
            NotificationChannels.PACKAGES,
            NotificationChannels.fromDeepLink("urbanguard://packages?packageId=1")
        )
        assertEquals(NotificationChannels.PQRS, NotificationChannels.fromDeepLink("urbanguard://pqrs/admin/1"))
        assertEquals(NotificationChannels.ANNOUNCEMENTS, NotificationChannels.fromDeepLink("urbanguard://community/1"))
        assertEquals(NotificationChannels.ANNOUNCEMENTS, NotificationChannels.fromDeepLink("urbanguard://news"))
        assertEquals(NotificationChannels.ANNOUNCEMENTS, NotificationChannels.fromDeepLink("urbanguard://unknown"))
        assertEquals(NotificationChannels.ANNOUNCEMENTS, NotificationChannels.fromDeepLink(null))
    }

    @Test
    fun `resolveChannel prioriza el canal explicito cuando es valido`() {
        assertEquals(
            NotificationChannels.PACKAGES,
            NotificationChannels.resolveChannel("PACKAGES", "urbanguard://news")
        )
        assertEquals(
            NotificationChannels.PATROL,
            NotificationChannels.resolveChannel(" patrol ", null)
        )
    }

    @Test
    fun `resolveChannel usa deepLink como fallback cuando el canal explicito es desconocido o nulo`() {
        assertEquals(
            NotificationChannels.REQUESTS,
            NotificationChannels.resolveChannel("desconocido", "urbanguard://requests/admin")
        )
        assertEquals(
            NotificationChannels.PQRS,
            NotificationChannels.resolveChannel(null, "urbanguard://pqrs/123")
        )
    }
}
