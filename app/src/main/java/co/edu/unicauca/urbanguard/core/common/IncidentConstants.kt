package co.edu.unicauca.urbanguard.core.common

/**
 * Constantes del reporte de novedades (UG-43).
 */
object IncidentConstants {

    /** Mínimo de caracteres de la descripción. */
    const val MIN_DESCRIPTION_LENGTH = 10

    // ── Mensajes ───────────────────────────────────────────────────────
    const val MSG_CATEGORY_REQUIRED = "Elige la categoría de la novedad."
    const val MSG_DESCRIPTION_REQUIRED = "Describe lo que está pasando."
    const val MSG_DESCRIPTION_TOO_SHORT =
        "Cuéntanos un poco más (mínimo $MIN_DESCRIPTION_LENGTH caracteres)."
    const val MSG_NO_ACTIVE_COMPLEX = "No tienes un conjunto activo seleccionado."
    const val MSG_SAVE_ERROR = "No se pudo guardar la novedad. Intenta de nuevo."
    const val MSG_LIST_ERROR = "No se pudieron cargar las novedades."
    const val MSG_CAMERA_ERROR = "No se pudo abrir la cámara."
    const val MSG_PHOTO_REQUIRED = "Toma una foto para poder reportar la novedad."
    const val LIST_EMPTY_TITLE = "Sin novedades reportadas"
    const val LIST_EMPTY_SUBTITLE =
        "Cuando veas algo raro en tu turno, repórtalo aquí con una foto."
}
