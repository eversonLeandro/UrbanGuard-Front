package co.edu.unicauca.urbanguard.core.common

/**
 * Constantes del registro de puntos de control NFC (UG-40).
 *
 * Textos de formulario, validación y estados vacíos. Los textos visibles de
 * la UI se escriben como literales en las pantallas, igual que en el resto
 * del proyecto; aquí solo los mensajes que se reutilizan o se testean.
 */
object PatrolConstants {

    /** Máximo de caracteres del nombre del punto. */
    const val MAX_NAME_LENGTH = 100

    /** Máximo de horarios por ruta de patrullaje. */
    const val MAX_SCHEDULES = 10

    /**
     * El backend ya expone los puntos de control como checkpoints
     * (`/api/complexes/{complexId}/checkpoints`).
     */
    const val PATROL_POINTS_REMOTE = true

    /** Rutas y rondas conectadas directamente al backend remoto. */
    const val PATROL_ROUTES_REMOTE = true
    const val PATROL_ROUNDS_REMOTE = true

    /** El historial por turnos se consulta mediante el API de seguridad. */
    const val PATROL_HISTORY_REMOTE = true

    /**
     * La ronda puede iniciarse este tiempo antes de su horario (ventana de
     * apertura). Más temprano aparece "Disponible desde…".
     */
    const val ROUND_EARLY_START_MINUTES = 10L

    // ── Mensajes de validación ───────────────────────────────────────────
    const val MSG_NAME_REQUIRED = "Ponle un nombre al punto (p. ej. Portería norte)."
    const val MSG_NAME_TOO_LONG = "El nombre no puede pasar de 100 caracteres."
    const val MSG_MAX_SCHEDULES = "Una ruta puede tener hasta 10 horarios."
    const val MSG_TAG_REQUIRED = "Acerca el tag NFC para vincularlo al punto."
    const val MSG_TAG_INVALID =
        "El UID debe tener 8, 14 o 20 caracteres hexadecimales."
    const val MSG_NO_ACTIVE_COMPLEX = "No tienes un conjunto activo seleccionado."
    const val MSG_SAVE_ERROR = "No se pudo guardar el punto. Intenta de nuevo."
    const val MSG_LIST_ERROR = "No se pudieron cargar los puntos de control."
    const val MSG_DELETE_ROUTE_ERROR = "No se pudo eliminar la ruta. Intenta de nuevo."
    const val MSG_DUPLICATE_TAG = "Ese tag ya está vinculado a otro punto."

    // ── Historial / lista ────────────────────────────────────────────────
    const val LIST_EMPTY_TITLE = "Aún no hay puntos de control"
    const val LIST_EMPTY_SUBTITLE =
        "Registra el primer punto acercando un tag NFC para armar la ruta."

    // ── Lector NFC ───────────────────────────────────────────────────────
    const val NFC_NO_HARDWARE = "Este equipo no tiene lector NFC."
    const val NFC_WAITING = "Acerca el tag NFC al equipo…"
    const val NFC_READ_TITLE = "Tag vinculado"

    /** Descripción accesible del icono NFC. */
    const val CD_NFC_ICON = "Lector NFC"
}
