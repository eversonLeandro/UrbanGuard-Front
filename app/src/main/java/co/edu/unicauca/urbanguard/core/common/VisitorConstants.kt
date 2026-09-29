package co.edu.unicauca.urbanguard.core.common

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Constantes de la funcionalidad de pase QR para visitantes.
 *
 * Reúne los topes de negocio, el tamaño de render del QR y los mensajes de
 * validación de la pantalla. Los textos visibles de la UI se escriben como
 * literales, igual que en el resto de pantallas del proyecto.
 */
object VisitorConstants {

    /** Mínimo de invitados permitido por pase. */
    const val MIN_GUESTS = 1

    /** Máximo de invitados permitido por pase. */
    const val MAX_GUESTS = 5

    /** Lado en píxeles del bitmap QR renderizado en pantalla. */
    const val QR_BITMAP_SIZE_PX = 640

    /**
     * Contenido del QR simulado. El backend devuelve una cadena y el frontend
     * la dibuja; mientras el endpoint no exista se devuelve este valor fijo.
     */
    const val SAMPLE_QR_CODE = "URBANGUARD|VISITOR|2026-01-01T00:00:00|1"

    /** Formato largo con el que se muestran las fechas de la pantalla. */
    val DATE_LABEL_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "CO"))

    /**
     * Bandera del bypass de visitantes, equivalente a `OnboardingBypass.ENABLED`.
     *
     * El backend todavía no expone `POST api/visitors/authorizations`, así que
     * `VisitorRepositoryImpl` devuelve un pase simulado mientras este flag esté
     * en `false`. Para conectar el endpoint real basta con ponerlo en `true`:
     * la llamada ya está escrita en el repositorio y `VisitorApi` sigue
     * inyectada por Hilt, no hay que tocar el módulo de inyección.
     */
    const val VISITORS_BACKEND_ENABLED = false

    // ── Mensajes de validación ────────────────────────────────────────────
    const val MSG_NAME_REQUIRED = "Ingresa el nombre del visitante."
    const val MSG_GUESTS_RANGE = "El número de invitados debe estar entre $MIN_GUESTS y $MAX_GUESTS."
    const val MSG_DATE_BEFORE_TODAY = "La fecha no puede ser anterior a hoy."
    const val MSG_NO_ACTIVE_COMPLEX = "No tienes un conjunto activo seleccionado."
    const val MSG_GENERIC_ERROR = "Ocurrió un error al generar el pase QR."
    const val MSG_LIST_ERROR = "No se pudo cargar el historial de pases."

    /** Descripción accesible de la imagen QR mostrada. */
    const val CD_QR_IMAGE = "Código QR del pase de visitante"

    // ── Compartir ──────────────────────────────────────────────────────────
    const val SHARE_CHOOSER_TITLE = "Compartir pase"
    const val SHARE_FILE_NAME = "pase_visitante.png"

    // ── Historial de visitantes ────────────────────────────────────────────
    const val LIST_EMPTY_TITLE = "Aún no tienes pases QR"
    const val LIST_EMPTY_SUBTITLE = "Genera el primer pase para autorizar el ingreso de tus visitantes."

    /**
     * Carpeta dentro de `cacheDir` donde se escribe el PNG del pase para
     * compartirlo. Debe coincidir con el `cache-path` de `res/xml/file_paths.xml`.
     */
    const val QR_CACHE_DIR = "qr"
}
