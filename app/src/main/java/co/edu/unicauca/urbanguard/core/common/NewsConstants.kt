package co.edu.unicauca.urbanguard.core.common

import java.time.format.DateTimeFormatter
import java.util.Locale

object NewsConstants {

    const val TITLE_MAX_LENGTH = 100
    const val BODY_MAX_LENGTH = 2000
    const val ATTACHMENT_MAX_BYTES = 5L * 1024L * 1024L
    const val ATTACHMENT_NAME_MAX_LENGTH = 100
    const val ATTACHMENT_CACHE_DIR = "news_attachments"

    val NEWS_DATE_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "CO"))

    const val MSG_TITLE_REQUIRED = "El título es obligatorio."
    const val MSG_TITLE_TOO_LONG = "El título no puede superar $TITLE_MAX_LENGTH caracteres."
    const val MSG_BODY_REQUIRED = "El cuerpo es obligatorio."
    const val MSG_BODY_TOO_LONG = "El cuerpo no puede superar $BODY_MAX_LENGTH caracteres."
    const val MSG_INVALID_CHARS = "El texto contiene caracteres no permitidos."
    const val MSG_NO_ACTIVE_COMPLEX = "No tienes un conjunto activo seleccionado."
    const val MSG_PUBLISH_ERROR = "No se pudo publicar el comunicado."
    const val MSG_UPDATE_ERROR = "No se pudo actualizar el comunicado."
    const val MSG_DELETE_ERROR = "No se pudo eliminar el comunicado."
    const val MSG_ANNOUNCEMENT_REQUIRED = "El comunicado no es válido."
    const val MSG_LOAD_ERROR = "No se pudieron cargar los comunicados."
    const val MSG_EMPTY_TITLE = "Aún no hay comunicados"
    const val MSG_EMPTY_SUBTITLE = "Los comunicados oficiales aparecerán aquí."
    const val PUBLISH_BUTTON_TEXT = "Publicar comunicado"
    const val EDIT_SCREEN_TITLE = "Editar comunicado"
    const val EDIT_BUTTON_TEXT = "Editar"
    const val DELETE_BUTTON_TEXT = "Eliminar"
    const val TITLE_LABEL = "Título"
    const val BODY_LABEL = "Cuerpo"
    const val PUBLISH_ACTION = "Publicar"
    const val SAVE_ACTION = "Guardar cambios"
    const val NEWS_SCREEN_TITLE = "Comunicados"
    const val DELETE_CONFIRM_TITLE = "Eliminar comunicado"
    const val DELETE_CONFIRM_BODY = "¿Deseas eliminar este comunicado? Esta acción no se puede deshacer."
    const val DELETE_CONFIRM_ACTION = "Eliminar"
    const val CANCEL_ACTION = "Cancelar"
    const val EXISTING_ATTACHMENT_LABEL = "Adjunto actual"
    const val REMOVE_ATTACHMENT_TEXT = "Quitar adjunto"
    const val KEEP_ATTACHMENT_TEXT = "Conservar adjunto"
    const val ATTACH_BUTTON_TEXT = "Adjuntar archivo (opcional)"
    const val ATTACH_HELP_TEXT = "PDF, JPG o PNG, máximo 5 MB"
    const val ATTACH_INVALID_TYPE = "Solo se permiten archivos PDF, JPG o PNG."
    const val ATTACH_TOO_LARGE = "El archivo supera el tamaño máximo de 5 MB."
    const val ATTACH_INVALID_NAME = "El nombre del archivo no es válido."
    const val ATTACH_OPEN_ERROR = "No hay una aplicación para abrir este archivo."
    const val ATTACH_DOWNLOAD_ERROR = "No se pudo descargar el archivo."
    const val ATTACH_FORBIDDEN = "No tienes permiso para descargar este archivo."
    const val ATTACH_NOT_FOUND = "El archivo no fue encontrado."
}
