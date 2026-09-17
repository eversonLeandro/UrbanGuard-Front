package co.edu.unicauca.urbanguard.domain.model

// Solicitud de vinculación de un residente a un conjunto (UG-49).
data class LinkRequest(
    val id: String = "",
    val nombreSolicitante: String = "",
    val emailSolicitante: String = "",
    val conjuntoId: String = "",
    val conjuntoNombre: String = "",
    val fechaSolicitud: String = "",
    val codigoVinculacion: String = "",
    val unidad: String = "",
    // Se mantiene como String para no romper el flujo LinkByCode del residente
    // (que aún no tiene estado tipado). En la UI se parsea a LinkRequestState.
    val estado: String = "PENDIENTE"
)

enum class LinkRequestState {
    PENDIENTE,
    APROBADA,
    RECHAZADA;

    companion object {
        fun fromString(value: String?): LinkRequestState = when (value?.trim()?.uppercase()) {
            "APROBADA", "APROBADO", "APPROVED" -> APROBADA
            "RECHAZADA", "RECHAZADO", "REJECTED" -> RECHAZADA
            else -> PENDIENTE
        }
    }
}