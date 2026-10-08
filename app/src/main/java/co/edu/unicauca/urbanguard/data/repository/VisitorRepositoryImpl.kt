package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toVisitorInvitation
import co.edu.unicauca.urbanguard.data.mapper.toVisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.mapper.toVisitorValidationResult
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorValidationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.VisitorApi
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationResult
import co.edu.unicauca.urbanguard.domain.model.sortedForResident
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
     * Historial de invitaciones del residente en el conjunto activo.
     *
     * Llama a `GET api/complexes/{complexId}/visitor-invitations` y ordena
     * para la pantalla (activas y programadas primero).
     */
    override suspend fun getVisitorInvitations(
        complexId: String
    ): Result<List<VisitorInvitation>> {
        if (complexId.isBlank()) {
            return Result.failure(
                Exception(VisitorConstants.MSG_NO_ACTIVE_COMPLEX)
            )
        }
        return try {
            Result.success(
                api.getInvitations(complexId).map { it.toVisitorInvitation() }
                    .sortedForResident()
            )
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar invitaciones", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: DateTimeParseException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    /**
     * Recupera el código de una invitación para recompartirla.
     *
     * Llama a `GET .../{invitationId}/code`; si el backend responde 410 el
     * [NetworkErrorMapper] lo traduce al mensaje de negocio ("ya no está
     * disponible").
     */
    override suspend fun getInvitationCode(
        complexId: String,
        invitationId: String
    ): Result<String> {
        if (complexId.isBlank() || invitationId.isBlank()) {
            return Result.failure(
                Exception(VisitorConstants.MSG_NO_ACTIVE_COMPLEX)
            )
        }
        return try {
            Result.success(api.getInvitationCode(complexId, invitationId).code)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al recuperar código de invitación", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override fun lastGeneratedPass(): VisitorAuthorization? = lastGenerated

    private var selected: VisitorInvitation? = null

    override fun setSelectedInvitation(
        invitation: VisitorInvitation?
    ) {
        selected = invitation
    }

    override fun selectedInvitation(): VisitorInvitation? =
        selected

    /**
     * Valida un pase QR contra el backend (el pase se consume, un solo uso).
     *
     * Pre-valida el formato local (22 chars base64url) para no gastar una
     * llamada; el `reason` del backend se mapea a [VisitorValidationFailure]
     * y viaja en el mensaje del [AppException] como `FAILURE|mensaje` para que
     * el ViewModel lo recupere sin comparar textos de UI.
     */
    override suspend fun validateVisitorQr(
        code: String,
        complexId: String
    ): Result<VisitorValidationResult> {
        val trimmed = code.trim()
        if (complexId.isBlank()) {
            return Result.failure(
                AppException(
                    co.edu.unicauca.urbanguard.domain.model.AppError.Validation(
                        VisitorConstants.MSG_NO_ACTIVE_COMPLEX
                    )
                ).withFailure(VisitorValidationFailure.NO_ACTIVE_COMPLEX)
            )
        }
        if (!isValidCodeFormat(trimmed)) {
            return Result.failure(
                AppException(
                    co.edu.unicauca.urbanguard.domain.model.AppError.Validation(
                        "El código QR escaneado no tiene un formato válido."
                    )
                ).withFailure(VisitorValidationFailure.INVALID_FORMAT)
            )
        }
        return try {
            val response = api.validateVisitorQr(complexId, VisitorValidationRequestDto(trimmed))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(
                        AppException(
                            co.edu.unicauca.urbanguard.domain.model.AppError.ServerError(
                                co.edu.unicauca.urbanguard.domain.model.AppError.ServerError.MESSAGE
                            )
                        ).withFailure(VisitorValidationFailure.SERVER_ERROR)
                    )
                Result.success(body.toVisitorValidationResult())
            } else {
                Result.failure(mapValidationError(response.code(), response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            Result.failure(mapValidationError(e.code(), e.response()?.errorBody()?.string()))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al validar pase QR", e)
            Result.failure(
                AppException(
                    co.edu.unicauca.urbanguard.domain.model.AppError.NoConnection(
                        co.edu.unicauca.urbanguard.domain.model.AppError.NoConnection.MESSAGE
                    )
                ).withFailure(VisitorValidationFailure.NO_CONNECTION)
            )
        } catch (e: DateTimeParseException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    private fun isValidCodeFormat(code: String): Boolean {
        if (code.length != 22) return false
        return code.all { it.isLetterOrDigit() || it == '-' || it == '_' }
    }

    private fun sampleResponse(visitDate: String): VisitorQrResponseDto = VisitorQrResponseDto(
        invitationId = UUID.randomUUID().toString(),
        code = VisitorConstants.SAMPLE_QR_CODE,
        visitDate = visitDate
    )

    private companion object {
        const val TAG = "VisitorRepo"
    }
}
