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
import javax.inject.Inject
import retrofit2.HttpException

class VisitorRepositoryImpl @Inject constructor(
    private val api: VisitorApi
) : VisitorRepository {

    private var lastGenerated: VisitorAuthorization? = null

    /**
     * Genera el pase QR del visitante indicado.
     *
     * El backend todavía no expone `POST api/visitors/authorizations`. Mientras
     * [VisitorConstants.VISITORS_BACKEND_ENABLED] sea `false` se devuelve un pase
     * simulado con el mismo contrato de datos que usa el mapper; al poner el flag
     * en `true` se ejecuta la llamada real, sin tocar el módulo de Hilt.
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
        val request = toVisitorQrRequestDto(visitorName, guestCount, date, complexId)
        return try {
            val authorization = if (VisitorConstants.VISITORS_BACKEND_ENABLED) {
                val response: VisitorQrResponseDto = api.generateVisitorQr(request)
                response.toDomain(visitorName = request.visitorName, guestCount = request.guestCount)
            } else {
                sampleResponse(request.date).toDomain(
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
     * Con el bypass activo se devuelve una lista vacía en lugar de invocar el
     * `GET`, que todavía no existe: así la pantalla de visitantes muestra su
     * estado vacío en vez de un error de red. Al activar el flag se consulta el
     * endpoint real.
     */
    override suspend fun getVisitorAuthorizations(): Result<List<VisitorAuthorization>> {
        if (!VisitorConstants.VISITORS_BACKEND_ENABLED) {
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

    private fun sampleResponse(date: String): VisitorQrResponseDto = VisitorQrResponseDto(
        qrCode = VisitorConstants.SAMPLE_QR_CODE,
        date = date
    )
}
