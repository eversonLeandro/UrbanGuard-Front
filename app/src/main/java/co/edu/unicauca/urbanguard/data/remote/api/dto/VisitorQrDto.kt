package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo para crear un pase QR de visitante.
 *
 * Endpoint PROVISIONAL: POST api/visitors/authorizations
 * Body: { "visitorName": String, "guestCount": Int, "date": "yyyy-MM-dd", "complexId": String }
 *
 * NOTA: el backend hace parseo estricto del cuerpo, por eso los nombres JSON
 * deben coincidir exactamente. El contrato está PENDIENTE de acordar con backend.
 */
data class VisitorQrRequestDto(
    val visitorName: String,
    val guestCount: Int,
    val date: String,
    val complexId: String
)

/**
 * Respuesta de la creación de un pase QR de visitante.
 *
 * Endpoint PROVISIONAL: POST api/visitors/authorizations
 * Éxito esperado: 201 Created
 * Body: { "qrCode": String, "date": "yyyy-MM-dd" }
 *
 * `qrCode` es el contenido que el frontend dibuja con ZXing; `date` llega como
 * texto ISO (yyyy-MM-dd) y se convierte a LocalDate en el mapper.
 * El contrato está PENDIENTE de acordar con backend.
 */
data class VisitorQrResponseDto(
    val qrCode: String,
    val date: String
)

/**
 * Elemento del historial de pases QR de visitante.
 *
 * Endpoint PROVISIONAL: GET api/visitors/authorizations
 * Body: [ { "id": ..., "visitorName": String, "guestCount": Int, "qrCode": String, "date": "yyyy-MM-dd" } ]
 *
 * El alta (POST) solo devuelve `qrCode` y `date`, así que el listado necesita
 * además el nombre y la cantidad de invitados para poder describir el pase.
 * Ese contrato ampliado está PENDIENTE de acordar con backend.
 */
data class VisitorQrItemDto(
    val id: String,
    val visitorName: String,
    val guestCount: Int,
    val qrCode: String,
    val date: String
)
