package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import java.time.LocalDate

interface VisitorRepository {

    /**
     * Crea un pase QR temporal para los visitantes indicados.
     *
     * Endpoint PROVISIONAL: `POST api/visitors/authorizations`.
     * El contrato (ruta, cuerpo y respuesta) está PENDIENTE de acordar con el
     * equipo de backend: todavía no existe tal endpoint en el API Gateway.
     *
     * - [visitorName] nombre del visitante, obligatorio.
     * - [guestCount] cantidad de invitados, entre 1 y 5.
     * - [date] día de vigencia elegido por el residente (yyyy-MM-dd).
     * - [complexId] conjunto autorizante, tomado de la sesión activa.
     *
     * El pase creado queda disponible en [lastGeneratedPass] para que la
     * pantalla de resultado no dependa de argumentos de navegación.
     */
    suspend fun generateVisitorQr(
        visitorName: String,
        guestCount: Int,
        date: LocalDate,
        complexId: String
    ): Result<VisitorAuthorization>

    /**
     * Lista los pases QR del conjunto activo para el historial de la pantalla
     * de visitantes.
     *
     * Endpoint PROVISIONAL: `GET api/visitors/authorizations`.
     * El cuerpo de la respuesta también está PENDIENTE de acordar con backend:
     * se asume que cada elemento trae `id`, `visitorName`, `guestCount`,
     * `qrCode` y `date`.
     */
    suspend fun getVisitorAuthorizations(): Result<List<VisitorAuthorization>>

    /**
     * Último pase generado en esta sesión, o null si todavía no se ha generado
     * ninguno. Vive en el repositorio (un @Singleton) para que la pantalla de
     * resultado pueda leerlo sin ViewModel compartido ni argumentos de ruta.
     */
    fun lastGeneratedPass(): VisitorAuthorization?
}
