package co.edu.unicauca.urbanguard.ui.navigation

private const val VISITORS_PREFIX = "urbanguard://visitors"
private const val REQUESTS_PREFIX = "urbanguard://requests"
private const val PATROL_PREFIX = "urbanguard://patrol"

/**
 * Traduce un deepLink del backend (`urbanGuard://...`) a la ruta interna.
 *
 * Lo usan tanto [co.edu.unicauca.urbanguard.MainActivity] (deep link entrante
 * del push) como la bandeja in-app al tocar una notificación, para que ambos
 * caminos lleven al mismo destino.
 */
fun routeForDeepLink(deepLink: String?): String? = when {
    deepLink == null -> null
    deepLink.startsWith(VISITORS_PREFIX) -> Routes.VISITORS
    deepLink.startsWith(REQUESTS_PREFIX) -> Routes.CONJUNTO_SELECTION
    // HU-A: el aviso de recorrido no completado abre la bandeja. El detalle de
    // la ronda (urbanguard://patrol/{roundId}) queda pendiente de un endpoint
    // GET por id en el backend; hasta entonces no hay pantalla de destino.
    deepLink.startsWith(PATROL_PREFIX) -> Routes.NOTIFICATIONS
    else -> null
}
