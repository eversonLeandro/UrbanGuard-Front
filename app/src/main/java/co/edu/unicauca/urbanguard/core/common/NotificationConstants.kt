package co.edu.unicauca.urbanguard.core.common

/**
 * Constantes de la bandeja de notificaciones in-app: tamaño de página y
 * mensajes visibles.
 */
object NotificationConstants {

    /** Avisos por página que pide la app al backend. */
    const val PAGE_SIZE = 20

    const val LIST_EMPTY_TITLE = "No tienes notificaciones"
    const val LIST_EMPTY_SUBTITLE = "Aquí verás los avisos de vinculación, visitantes y recorridos."

    const val MSG_LIST_ERROR = "No se pudieron cargar las notificaciones."
    const val MSG_MARK_READ_ERROR = "No se pudo marcar la notificación como leída."
}
