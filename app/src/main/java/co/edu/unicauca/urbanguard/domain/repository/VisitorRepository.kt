package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationResult
import java.time.LocalDate

interface VisitorRepository {

    /**
     * Crea un pase QR temporal para los visitantes indicados.
     *
     * `POST api/complexes/{complexId}/visitor-invitations` con body
     * `{ visitorName, guestCount, visitDate }` (parseo estricto, sin campos
     * extra); éxito 201 Created con `{ invitationId, code, visitDate }`, donde
     * `code` es el contenido del QR.
     *
     * - [visitorName] nombre del visitante, obligatorio.
     * - [guestCount] cantidad de invitados, entre 1 y 5.
     * - [date] día de vigencia elegido por el residente (yyyy-MM-dd).
     * - [complexId] conjunto autorizante, tomado de la sesión activa (va en la
     *   ruta de la petición, no en el body).
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
     * Lista las invitaciones creadas por el residente en el conjunto activo,
     * con su estado funcional (activa, programada, usada o expirada).
     *
     * `GET api/complexes/{complexId}/visitor-invitations`; el `complexId` va
     * en la ruta y se toma de la sesión activa.
     */
    suspend fun getVisitorInvitations(complexId: String): Result<List<VisitorInvitation>>

    /**
     * Recupera el código QR de una invitación activa o programada para
     * volver a compartirla.
     *
     * `GET api/complexes/{complexId}/visitor-invitations/{invitationId}/code`;
     * si ya no está disponible (usada o expirada) falla con 410.
     */
    suspend fun getInvitationCode(
        complexId: String,
        invitationId: String
    ): Result<String>

    /**
     * Invitación elegida en el listado para recompartir, o null si no hay
     * ninguna. Vive en el repositorio (un @Singleton) para que la pantalla de
     * detalle no necesite argumentos de navegación, igual que
     * [lastGeneratedPass].
     */
    fun setSelectedInvitation(invitation: VisitorInvitation?)

    fun selectedInvitation(): VisitorInvitation?

    /**
     * Valida un pase QR de visitante (flujo guardia, un solo uso).
     *
     * `POST api/complexes/{complexId}/visitor-invitations/validation` con body
     * `{ code }`; éxito 200 con los datos de la invitación consumida.
     *
     * El `reason` del error se conserva tipado en [VisitorValidationFailure]
     * para que el ViewModel decida la tarjeta a mostrar.
     */
    suspend fun validateVisitorQr(
        code: String,
        complexId: String
    ): Result<VisitorValidationResult>

    /**
     * Último pase generado en esta sesión, o null si todavía no se ha generado
     * ninguno. Vive en el repositorio (un @Singleton) para que la pantalla de
     * resultado pueda leerlo sin ViewModel compartido ni argumentos de ruta.
     */
    fun lastGeneratedPass(): VisitorAuthorization?
}
