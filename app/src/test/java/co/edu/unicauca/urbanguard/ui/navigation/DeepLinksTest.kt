package co.edu.unicauca.urbanguard.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * El mapeo de deep links del backend a rutas internas. HU-A: el aviso de
 * recorrido no completado abre la bandeja porque aún no existe un endpoint GET
 * por id de ronda. El resto de destinos no cambia.
 */
class DeepLinksTest {

    @Test
    fun `patrol abre la bandeja de notificaciones`() {
        assertEquals(Routes.NOTIFICATIONS, routeForDeepLink("urbanguard://patrol/abc-123"))
    }

    @Test
    fun `visitors conserva su destino`() {
        assertEquals(Routes.VISITORS, routeForDeepLink("urbanguard://visitors/42"))
    }

    @Test
    fun `requests conserva su destino`() {
        assertEquals(Routes.CONJUNTO_SELECTION, routeForDeepLink("urbanguard://requests/7"))
    }

    @Test
    fun `un deepLink desconocido no navega`() {
        assertNull(routeForDeepLink("urbanguard://unknown/1"))
    }

    @Test
    fun `un deepLink ausente no navega`() {
        assertNull(routeForDeepLink(null))
    }
}
