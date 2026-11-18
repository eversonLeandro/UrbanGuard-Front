package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.domain.model.PatrolComplianceLevel
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundExecutionStatus
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Textos y tonos del detalle de una ronda (nivel 3).
 *
 * Este nivel es detalle completo, así que los instantes se muestran con fecha
 * civil y hora ("13 de enero de 2026, 08:45 a. m.") en la zona del condominio;
 * para la ventana de horario se usa la fecha corta para no duplicar texto.
 */
object PatrolRoundDetailFormat {

    private val zone: ZoneId = PatrolHistoryConstants.CONDOMINIUM_ZONE
    private val locale: Locale = Locale.forLanguageTag("es-CO")

    /** Fecha y hora completas para instantes clave ("13 de enero de 2026, 08:45 a. m."). */
    private val fullFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, hh:mm a", locale).withZone(zone)

    /** Fecha corta + hora para la ventana ("12/01/26, 7:30 p. m."). */
    private val shortFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yy, h:mm a", locale).withZone(zone)

    /** Fecha y hora completas del instante, o "—" si no ocurrió. */
    fun fullDateTimeOrDash(instant: Instant?): String =
        instant?.let { normalize(fullFormat.format(it)) } ?: "—"

    /** Fecha corta + hora, o "—" si no viene. */
    fun shortDateTimeOrDash(instant: Instant?): String =
        instant?.let { normalize(shortFormat.format(it)) } ?: "—"

    /** Hora de escaneo de un punto, o "Sin escanear" para el punto omitido. */
    fun checkpointScannedAt(scannedAt: Instant?): String =
        if (scannedAt != null) normalize(fullFormat.format(scannedAt)) else "Sin escanear"

    /** Etiqueta del estado de ejecución de la ronda. */
    fun statusLabel(status: PatrolRoundExecutionStatus): String = when (status) {
        PatrolRoundExecutionStatus.SCHEDULED -> "PROGRAMADA"
        PatrolRoundExecutionStatus.IN_PROGRESS -> "EN CURSO"
        PatrolRoundExecutionStatus.COMPLETED -> "COMPLETADA"
        PatrolRoundExecutionStatus.INCOMPLETE -> "INCOMPLETA"
        PatrolRoundExecutionStatus.NOT_PERFORMED -> "NO REALIZADA"
    }

    /** Tono del badge del estado de ejecución. */
    fun statusTone(status: PatrolRoundExecutionStatus): StatusTone = when (status) {
        PatrolRoundExecutionStatus.COMPLETED -> StatusTone.SUCCESS
        PatrolRoundExecutionStatus.INCOMPLETE,
        PatrolRoundExecutionStatus.NOT_PERFORMED -> StatusTone.ERROR
        PatrolRoundExecutionStatus.IN_PROGRESS -> StatusTone.WARNING
        PatrolRoundExecutionStatus.SCHEDULED -> StatusTone.NEUTRAL
    }

    /** Etiqueta del nivel de cumplimiento de la ronda. */
    fun complianceLabel(compliance: PatrolComplianceLevel): String = when (compliance) {
        PatrolComplianceLevel.COMPLIANT -> "CUMPLE"
        PatrolComplianceLevel.PENDING -> "PENDIENTE"
        PatrolComplianceLevel.NON_COMPLIANT -> "NO CUMPLE"
    }

    /** Tono del badge del nivel de cumplimiento. */
    fun complianceTone(compliance: PatrolComplianceLevel): StatusTone = when (compliance) {
        PatrolComplianceLevel.COMPLIANT -> StatusTone.SUCCESS
        PatrolComplianceLevel.PENDING -> StatusTone.WARNING
        PatrolComplianceLevel.NON_COMPLIANT -> StatusTone.ERROR
    }

    private fun normalize(value: String): String =
        value.replace('\u202f', ' ').replace('\u00a0', ' ')
}
