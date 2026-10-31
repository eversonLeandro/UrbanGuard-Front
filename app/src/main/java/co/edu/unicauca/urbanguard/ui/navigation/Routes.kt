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
    const val ADMIN_INCIDENTS = "admin_incidents"
    const val ADMIN_INCIDENT_DETAIL = "admin_incident_detail/{reportId}"
    const val NEWS = "news"
    const val PROFILE = "profile"
    const val VISITORS = "visitors"
    const val VISITORS_NEW_PASS = "visitors/new_pass"
    const val VISITORS_PASS = "visitors/pass"
    const val VISITORS_PASS_DETAIL = "visitors/pass_detail"
    const val SUPPORT = "support"
    const val PQRS_INBOX = "pqrs_inbox"
    const val PQRS_DETAIL = "pqrs_detail/{pqrsId}"
    const val MY_PQRS = "my_pqrs"
    const val CREATE_PQRS = "create_pqrs"
    const val MY_PQRS_DETAIL = "my_pqrs_detail/{pqrsId}"
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
    const val PATROL_POINTS = "patrol_points"
    const val PATROL_POINT_FORM = "patrol_point_form?pointId={pointId}"
    const val PATROL_ROUTES = "patrol_routes"
    const val PATROL_ROUTE_FORM = "patrol_route_form?routeId={routeId}"
    const val PATROL_SHIFT_DETAIL =
        "patrol_shift_detail?shiftType={shiftType}&date={date}&from={from}&to={to}"
    const val PATROL_GUARD_SUMMARY = "patrol_guard_summary?guardId={guardId}&from={from}&to={to}"

    // Rutas del Vigilante (barra de navegación inferior del rol GUARD)
    const val GUARD_HOME = "guard_home"
    const val GUARD_VISITANTES = "guard_visitantes"
    const val GUARD_SEGURIDAD = "guard_seguridad"
    const val GUARD_PERFIL = "guard_perfil"
    const val GUARD_SCAN_QR = "guard_scan_qr"
    const val GUARD_ROUND_ACTIVE = "guard_round_active"
    const val GUARD_ROUND_SUMMARY = "guard_round_summary"
    const val GUARD_INCIDENTS = "guard_incidents"
    const val GUARD_INCIDENT_FORM =
        "guard_incident_form?pointId={pointId}&executionId={executionId}"
    const val GUARD_INCIDENT_DETAIL = "guard_incident_detail/{reportId}"

    const val PUBLISH_NEWS = "publish_news"
}
