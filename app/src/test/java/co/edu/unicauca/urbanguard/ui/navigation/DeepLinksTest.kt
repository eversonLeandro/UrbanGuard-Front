package co.edu.unicauca.urbanguard.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * El mapeo de deep links del backend a rutas internas. HU-A: el aviso de
 * recorrido no completado abre la bandeja porque aún no existe un endpoint GET
 * por id de ronda. El cierre de PQRS abre el detalle de esa PQRS.
 */
class DeepLinksTest {

    private val pqrsId = "3c55f0f4-8d2b-4a1e-9f7c-1b2c3d4e5f60"

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
    fun `pqrs abre el detalle de esa pqrs`() {
        assertEquals(
            "my_pqrs_detail/$pqrsId",
            routeForDeepLink("urbanguard://pqrs/$pqrsId")
        )
    }

    @Test
    fun `pqrs ignora parametros de consulta`() {
        assertEquals(
            "my_pqrs_detail/$pqrsId",
            routeForDeepLink("urbanguard://pqrs/$pqrsId?status=RESOLVED")
        )
    }

    @Test
    fun `pqrs nueva abre el detalle administrativo`() {
        assertEquals(
            "pqrs_detail/$pqrsId",
            routeForDeepLink("urbanguard://pqrs/admin/$pqrsId")
        )
    }

    @Test
    fun `pqrs administrativa ignora parametros de consulta`() {
        assertEquals(
            "pqrs_detail/$pqrsId",
            routeForDeepLink("urbanguard://pqrs/admin/$pqrsId?status=SUBMITTED")
        )
    }

    @Test
    fun `pqrs administrativa sin id utilizable abre la bandeja`() {
        assertEquals(Routes.PQRS_INBOX, routeForDeepLink("urbanguard://pqrs/admin"))
        assertEquals(Routes.PQRS_INBOX, routeForDeepLink("urbanguard://pqrs/admin/"))
        assertEquals(Routes.PQRS_INBOX, routeForDeepLink("urbanguard://pqrs/admin/no-es-uuid"))
    }

    @Test
    fun `un pqrs sin id utilizable abre la lista de pqrs`() {
        assertEquals(Routes.MY_PQRS, routeForDeepLink("urbanguard://pqrs/"))
        assertEquals(Routes.MY_PQRS, routeForDeepLink("urbanguard://pqrs"))
        assertEquals(Routes.MY_PQRS, routeForDeepLink("urbanguard://pqrs/no-es-uuid"))
    }

    @Test
    fun `un deepLink desconocido no navega`() {
        assertNull(routeForDeepLink("urbanguard://unknown/1"))
    }

    @Test
    fun `un deepLink ausente no navega`() {
        assertNull(routeForDeepLink(null))
    }

    @Test
    fun `news deep link se mapea al feed de comunicados`() {
        assertEquals(Routes.NEWS, routeForDeepLink("urbanguard://news"))
    }
}
