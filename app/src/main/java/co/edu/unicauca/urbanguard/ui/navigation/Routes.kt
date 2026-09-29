package co.edu.unicauca.urbanguard.ui.navigation

// Rutas de navegación — una constante por pantalla
object Routes {
    const val LOGIN = "login"
    const val CONJUNTO_CODE = "conjunto_code"
    const val ACTIVATE_CONJUNTO = "activate_conjunto"
    const val REGISTER_CONJUNTO = "register_conjunto/{activationCode}"
    const val PENDING_REQUESTS = "pending_requests"
    const val REQUEST_DETAIL = "link_request_detail/{requestId}"
    const val HOME = "home"
    const val CENSUS = "census"
    const val SECURITY = "security"
    const val NEWS = "news"
    const val PROFILE = "profile"
    const val VISITORS = "visitors"
    const val VISITORS_NEW_PASS = "visitors/new_pass"
    const val VISITORS_PASS = "visitors/pass"
    const val SUPPORT = "support"
    const val CONJUNTO_INFO = "conjunto_info"
    const val RESERVATIONS = "reservations"
    const val PACKAGES = "packages"
    const val NOTIFICATIONS = "notifications"

    // Pendiente: agregar el resto de rutas aquí a medida que se implementen.
    const val LINK_BY_CODE = "link_by_code"
    const val REGISTER = "register"
    const val CONJUNTO_SELECTION = "conjunto_selection"
    const val ADMIN = "admin"
    const val REGISTER_GUARD = "register_guard"
    const val PATROL_HISTORY = "patrol_history"

    // Rutas del Vigilante (barra de navegación inferior del rol GUARD)
    const val GUARD_HOME = "guard_home"
    const val GUARD_VISITANTES = "guard_visitantes"
    const val GUARD_SEGURIDAD = "guard_seguridad"
    const val GUARD_PERFIL = "guard_perfil"
    const val GUARD_SCAN_QR = "guard_scan_qr"
}
