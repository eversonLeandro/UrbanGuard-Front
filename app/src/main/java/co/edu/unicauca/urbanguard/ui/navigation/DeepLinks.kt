package co.edu.unicauca.urbanguard.ui.navigation

private const val VISITORS_PREFIX = "urbanguard://visitors"
private const val REQUESTS_PREFIX = "urbanguard://requests"
private const val REQUESTS_ADMIN_PREFIX = "$REQUESTS_PREFIX/admin"
private const val PATROL_REPORTS_PREFIX = "urbanguard://patrol-reports"
private const val PATROL_REPORTS_DETAIL_PREFIX = "$PATROL_REPORTS_PREFIX/"
private const val PATROL_GUARD_PREFIX = "urbanguard://patrol/guard"
private const val PATROL_PREFIX = "urbanguard://patrol"
private const val PACKAGES_PREFIX = "urbanguard://packages"
private const val NEWS_PREFIX = "urbanguard://news"
private const val PQRS_PREFIX = "urbanguard://pqrs"
private const val PQRS_DETAIL_PREFIX = "$PQRS_PREFIX/"
private const val PQRS_ADMIN_PREFIX = "$PQRS_PREFIX/admin"
private const val PQRS_ADMIN_DETAIL_PREFIX = "$PQRS_ADMIN_PREFIX/"
private val UUID_REGEX = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

/**
 * Traduce un deepLink del backend (`urbanguard://...`) a la ruta interna.
 *
 * Lo usan tanto [co.edu.unicauca.urbanguard.MainActivity] (deep link entrante
 * del push) como la bandeja in-app y el banner flotante al tocar una
 * notificación, para que todos los caminos lleven al mismo destino.
 */
fun routeForDeepLink(deepLink: String?): String? = when {
    deepLink == null -> null
    deepLink.startsWith(VISITORS_PREFIX) -> Routes.VISITORS
    isAdminRequestsDeepLink(deepLink) -> Routes.PENDING_REQUESTS
    deepLink.startsWith(REQUESTS_PREFIX) -> Routes.CONJUNTO_SELECTION
    deepLink.startsWith(PATROL_REPORTS_PREFIX) -> patrolReportDetailRoute(deepLink)
    deepLink.startsWith(PATROL_GUARD_PREFIX) -> Routes.GUARD_SEGURIDAD
    deepLink.startsWith(PATROL_PREFIX) -> Routes.NOTIFICATIONS
    deepLink.startsWith(PACKAGES_PREFIX) -> Routes.PACKAGES
    deepLink.startsWith(NEWS_PREFIX) -> Routes.NEWS
    isAdminPqrsDeepLink(deepLink) -> pqrsAdminDetailRoute(deepLink)
    deepLink.startsWith(PQRS_PREFIX) -> pqrsDetailRoute(deepLink)
    else -> null
}

private fun isAdminRequestsDeepLink(deepLink: String): Boolean =
    deepLink == REQUESTS_ADMIN_PREFIX ||
        deepLink.startsWith("$REQUESTS_ADMIN_PREFIX/") ||
        deepLink.startsWith("$REQUESTS_ADMIN_PREFIX?")

private fun patrolReportDetailRoute(deepLink: String): String {
    val id = deepLink.removePrefix(PATROL_REPORTS_DETAIL_PREFIX).substringBefore('?').trim('/')
    return if (UUID_REGEX.matches(id)) {
        Routes.ADMIN_INCIDENT_DETAIL.replace("{reportId}", id)
    } else {
        Routes.ADMIN_INCIDENTS
    }
}

private fun isAdminPqrsDeepLink(deepLink: String): Boolean =
    deepLink == PQRS_ADMIN_PREFIX ||
        deepLink.startsWith(PQRS_ADMIN_DETAIL_PREFIX) ||
        deepLink.startsWith("$PQRS_ADMIN_PREFIX?")

private fun pqrsAdminDetailRoute(deepLink: String): String {
    val id = deepLink.removePrefix(PQRS_ADMIN_DETAIL_PREFIX).substringBefore('?').trim('/')
    return if (UUID_REGEX.matches(id)) {
        Routes.PQRS_DETAIL.replace("{pqrsId}", id)
    } else {
        Routes.PQRS_INBOX
    }
}

/**
 * Sin un `pqrsId` con forma de UUID no se puede abrir el detalle: se degrada a
 * la lista de PQRS propias en vez de navegar a una ruta inválida.
 */
private fun pqrsDetailRoute(deepLink: String): String {
    val id = deepLink.removePrefix(PQRS_DETAIL_PREFIX).substringBefore('?').trim('/')
    return if (UUID_REGEX.matches(id)) {
        Routes.MY_PQRS_DETAIL.replace("{pqrsId}", id)
    } else {
        Routes.MY_PQRS
    }
}
