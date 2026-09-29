package co.edu.unicauca.urbanguard.domain.model

import java.time.LocalDate

/**
 * Pase temporal autorizado por un residente para el ingreso de visitantes.
 *
 * [qrCode] es el contenido que el backend devuelve y que el frontend dibuja
 * como imagen QR; [validDate] es el día elegido por el residente, a partir del
 * cual el pase queda vigente.
 *
 * [visitorName] y [guestCount] acompañan al pase para que el listado de la
 * pantalla de visitantes pueda describirlo y para componer el texto que se
 * comparte. En el alta provienen de la solicitud enviada y en el listado del
 * cuerpo que devuelve el backend.
 */
data class VisitorAuthorization(
    val qrCode: String,
    val validDate: LocalDate,
    val visitorName: String,
    val guestCount: Int
)
