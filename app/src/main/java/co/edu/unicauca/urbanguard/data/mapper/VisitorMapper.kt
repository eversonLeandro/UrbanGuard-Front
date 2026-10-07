package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrItemDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import java.time.LocalDate

/**
 * Construye el body de POST `api/complexes/{complexId}/visitor-invitations` a
 * partir de los datos del formulario. La fecha viaja como `visitDate` en texto
 * ISO (yyyy-MM-dd), que es el formato que espera el backend; el `complexId` no
 * forma parte del body porque va en la ruta.
 */
fun toVisitorQrRequestDto(
    visitorName: String,
    guestCount: Int,
    date: LocalDate
): VisitorQrRequestDto = VisitorQrRequestDto(
    visitorName = visitorName,
    guestCount = guestCount,
    visitDate = date.toString()
)

/**
 * Convierte la respuesta de POST `api/complexes/{complexId}/visitor-invitations`
 * (201 Created) en el modelo interno de dominio.
 *
 * `code` es el contenido del QR, por eso se mapea a [VisitorAuthorization.qrCode].
 * La respuesta no trae el nombre ni la cantidad de invitados, así que se toman
 * de la misma solicitud que originó el pase, para que la pantalla de resultado
 * pueda describirlo y compartirlo.
 *
 * Lanza [java.time.format.DateTimeParseException] si `visitDate` no viene en
 * formato ISO; el repositorio captura ese error explícitamente.
 */
fun VisitorQrResponseDto.toDomain(
    visitorName: String,
    guestCount: Int
): VisitorAuthorization = VisitorAuthorization(
    qrCode = code,
    validDate = LocalDate.parse(visitDate),
    visitorName = visitorName,
    guestCount = guestCount
)

/**
 * Convierte un elemento del historial de GET /api/visitors/authorizations en
 * el modelo interno de dominio.
 *
 * Lanza [java.time.format.DateTimeParseException] si `date` no viene en formato
 * ISO; el repositorio captura ese error explícitamente.
 */
fun VisitorQrItemDto.toDomain(): VisitorAuthorization = VisitorAuthorization(
    qrCode = qrCode,
    validDate = LocalDate.parse(date),
    visitorName = visitorName,
    guestCount = guestCount
)
