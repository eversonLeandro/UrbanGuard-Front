package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toVisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.service.VisitorApi
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class VisitorRepositoryImpl @Inject constructor(
    private val api: VisitorApi
) : VisitorRepository {

    private var lastGenerated: VisitorAuthorization? = null

    /**
     * Genera el pase QR del visitante indicado.
     *
     * Mientras [VisitorConstants.VISITORS_BACKEND_ENABLED] sea `false` se
     * devuelve un pase simulado con el mismo contrato de datos que usa el
     * mapper; con el flag en `true` se llama a
     * `POST api/complexes/{complexId}/visitor-invitations`, sin tocar el módulo
     * de Hilt.
     *
     * El pase creado o simulado queda en [lastGeneratedPass] para que la
     * pantalla de resultado lo lea sin argumentos de navegación.
     */
    override suspend fun generateVisitorQr(
        visitorName: String,
        guestCount: Int,
        date: LocalDate,
        complexId: String
    ): Result<VisitorAuthorization> {
        val request = toVisitorQrRequestDto(visitorName, guestCount, date)
        return try {
            val authorization = if (VisitorConstants.VISITORS_BACKEND_ENABLED) {
                val response: VisitorQrResponseDto = api.generateVisitorQr(complexId, request)
                response.toDomain(visitorName = request.visitorName, guestCount = request.guestCount)
            } else {
                sampleResponse(request.visitDate).toDomain(
                    visitorName = request.visitorName,
                    guestCount = request.guestCount
                )
            }
            lastGenerated = authorization
            Result.success(authorization)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: DateTimeParseException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    /**
     * Historial de pases del conjunto activo.
     *
     * Mientras [VisitorConstants.VISITOR_HISTORY_BACKEND_ENABLED] sea `false`
     * se devuelve una lista vacía sin invocar el `GET`, cuyo contrato con el
     * backend todavía no está adaptado: así la pantalla de visitantes muestra
     * su estado vacío en vez de un error de red. Al activar el flag se consulta
     * el endpoint real.
     */
    override suspend fun getVisitorAuthorizations(): Result<List<VisitorAuthorization>> {
        if (!VisitorConstants.VISITOR_HISTORY_BACKEND_ENABLED) {
            return Result.success(emptyList())
        }
        return try {
            Result.success(api.getVisitorAuthorizations().map { it.toDomain() })
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: DateTimeParseException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override fun lastGeneratedPass(): VisitorAuthorization? = lastGenerated

    private fun sampleResponse(visitDate: String): VisitorQrResponseDto = VisitorQrResponseDto(
        invitationId = UUID.randomUUID().toString(),
        code = VisitorConstants.SAMPLE_QR_CODE,
        visitDate = visitDate
    )
}
