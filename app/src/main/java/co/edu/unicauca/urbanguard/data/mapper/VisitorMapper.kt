package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrItemDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import java.time.LocalDate

/**
 * Construye el body de POST /api/visitors/authorizations a partir de los datos
 * del formulario. La fecha viaja como texto ISO (yyyy-MM-dd), que es el formato
 * que espera el backend.
 */
fun toVisitorQrRequestDto(
    visitorName: String,
    guestCount: Int,
    date: LocalDate,
    complexId: String
): VisitorQrRequestDto = VisitorQrRequestDto(
    visitorName = visitorName,
    guestCount = guestCount,
    date = date.toString(),
    complexId = complexId
)

/**
 * Convierte la respuesta del endpoint POST /api/visitors/authorizations
 * (201 Created) en el modelo interno de dominio.
 *
 * La respuesta solo trae `qrCode` y `date`, así que el nombre y la cantidad de
 * invitados se toman de la misma solicitud que originó el pase, para que la
 * pantalla de resultado pueda describirlo y compartirlo.
 *
 * Lanza [java.time.format.DateTimeParseException] si `date` no viene en formato
 * ISO; el repositorio captura ese error explícitamente.
 */
fun VisitorQrResponseDto.toDomain(
    visitorName: String,
    guestCount: Int
): VisitorAuthorization = VisitorAuthorization(
    qrCode = qrCode,
    validDate = LocalDate.parse(date),
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
