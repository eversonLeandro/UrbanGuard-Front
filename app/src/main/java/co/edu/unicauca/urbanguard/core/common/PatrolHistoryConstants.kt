package co.edu.unicauca.urbanguard.core.common

import java.time.ZoneId

/**
 * Constantes del historial de patrullaje por turno (HU-034 / HU-032).
 *
 * El backend es quien calcula turnos, cumplimiento y meta; aquí solo viven los
 * topes de la consulta (rango de fechas) y los mensajes que se reutilizan o se
 * testean. Los textos visibles de la UI se escriben como literales, igual que
 * en el resto de pantallas del proyecto.
 */
object PatrolHistoryConstants {

    /** Rango por defecto de la consulta: últimos 7 días (incluyendo hoy). */
    const val DEFAULT_RANGE_DAYS = 7

    /** Máximo de días que admite el backend en un rango, contando ambos extremos. */
    const val MAX_RANGE_DAYS = 31

    /**
     * Zona del condominio. Las fechas de turno se calculan aquí y los
     * horarios ISO-8601 con offset se muestran en esta zona.
     */
    val CONDOMINIUM_ZONE: ZoneId = ZoneId.of("America/Bogota")

    // ── Mensajes ─────────────────────────────────────────────────────────
    const val MSG_NO_ACTIVE_COMPLEX = "No tienes un conjunto activo seleccionado."
    const val MSG_LIST_ERROR = "No se pudo cargar el historial de turnos."
    const val MSG_GUARDS_FILTER_ERROR =
        "No se pudieron cargar los vigilantes; el filtro no está disponible."
    const val MSG_DETAIL_ERROR = "No se pudo cargar el detalle del turno."
    const val MSG_GUARD_SUMMARY_ERROR = "No se pudo cargar el resumen del vigilante."
    const val MSG_INVALID_SERVER_RESPONSE = "Respuesta inválida del servidor."

    /** Validación del filtro de fechas (mostrada en pantalla, no llama a la API). */
    const val MSG_RANGE_INVALID = "La fecha 'Desde' no puede ser posterior a 'Hasta'."
    const val MSG_RANGE_TOO_LONG = "El rango no puede superar los $MAX_RANGE_DAYS días."

    /** 404 con mensaje propio según el endpoint consultado. */
    const val MSG_SHIFT_NOT_FOUND = "Turno no encontrado."
    const val MSG_GUARD_NOT_FOUND = "Vigilante no encontrado."
    const val MSG_ROUND_NOT_FOUND = "Ronda no encontrada."

    /** Fallo genérico del detalle de ronda (sin mensaje del backend). */
    const val MSG_ROUND_DETAIL_ERROR = "No se pudo cargar el detalle de la ronda."

    /** Argumentos de navegación incompletos (no se puede consultar la ronda). */
    const val MSG_ROUND_ARGS = "No se pudo determinar la ronda a consultar."

    /** Argumentos de navegación incompletos (no se puede consultar el resumen). */
    const val MSG_GUARD_SUMMARY_ARGS = "No se pudo determinar el vigilante o el rango a consultar."

    /** Argumentos de navegación incompletos (no se puede consultar el turno). */
    const val MSG_DETAIL_ARGS = "No se pudo determinar el turno a consultar."
}
