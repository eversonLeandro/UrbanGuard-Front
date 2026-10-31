package co.edu.unicauca.urbanguard.ui.navigation

private const val VISITORS_PREFIX = "urbanguard://visitors"
private const val REQUESTS_PREFIX = "urbanguard://requests"
private const val PATROL_PREFIX = "urbanguard://patrol"
private const val PQRS_PREFIX = "urbanguard://pqrs"
private const val PQRS_DETAIL_PREFIX = "$PQRS_PREFIX/"
private val PQRS_ID = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

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
    // Aviso de cierre de PQRS: `urbanguard://pqrs/{pqrsId}` abre el detalle del
    // residente con el historial y la respuesta del administrador.
    deepLink.startsWith(PQRS_PREFIX) -> pqrsDetailRoute(deepLink)
    else -> null
}

/**
 * Sin un `pqrsId` con forma de UUID no se puede abrir el detalle: se degrada a
 * la lista de PQRS propias en vez de navegar a una ruta inválida.
 */
private fun pqrsDetailRoute(deepLink: String): String {
    val id = deepLink.removePrefix(PQRS_DETAIL_PREFIX).substringBefore('?').trim('/')
    return if (PQRS_ID.matches(id)) {
        Routes.MY_PQRS_DETAIL.replace("{pqrsId}", id)
    } else {
        Routes.MY_PQRS
    }
}
