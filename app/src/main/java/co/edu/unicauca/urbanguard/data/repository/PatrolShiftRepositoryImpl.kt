package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolShiftApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Historial de patrullaje por turno (HU-034 / HU-032).
 *
 * No hay cálculo en el frontend: cada método llama al backend y traduce el
 * contrato. `from`/`to` se mandan siempre como `yyyy-MM-dd`; `guardId` y
 * `onlyIncomplete` solo cuando tienen valor (Retrofit omite los `@Query` nulos).
 * El mapper falla toda la respuesta ante un dato inválido, así que ninguna
 * lista se recorta en silencio.
 */
class PatrolShiftRepositoryImpl @Inject constructor(
    private val api: PatrolShiftApi
) : PatrolShiftRepository {

    override suspend fun getShiftSummaries(
        complexId: String,
        from: LocalDate,
        to: LocalDate,
        guardId: String?,
        onlyIncomplete: Boolean,
        status: PatrolShiftStatus?
    ): Result<List<PatrolShiftSummary>> {
        if (complexId.isBlank()) return noComplexFailure()
        return apiCall {
            api.getShifts(
                complexId = complexId,
                from = from.toString(),
                to = to.toString(),
                guardId = guardId?.takeIf { it.isNotBlank() },
                onlyIncomplete = if (onlyIncomplete) true else null,
                status = status?.name
            ).map { it.toDomain() }
        }
    }

    override suspend fun getShiftDetail(
        complexId: String,
        shiftType: PatrolShiftType,
        date: LocalDate
    ): Result<PatrolShiftDetail> {
        if (complexId.isBlank()) return noComplexFailure()
        return apiCall {
            api.getShiftDetail(
                complexId = complexId,
                shiftType = shiftType.name,
                date = date.toString()
            ).toDomain()
        }.withNotFoundMessage(PatrolHistoryConstants.MSG_SHIFT_NOT_FOUND)
    }

    override suspend fun getGuardSummary(
        complexId: String,
        guardId: String,
        from: LocalDate,
        to: LocalDate
    ): Result<PatrolGuardSummary> {
        if (complexId.isBlank()) return noComplexFailure()
        return apiCall {
            api.getGuardSummary(
                complexId = complexId,
                guardId = guardId,
                from = from.toString(),
                to = to.toString()
            ).toDomain()
        }.withNotFoundMessage(PatrolHistoryConstants.MSG_GUARD_NOT_FOUND)
    }

    private fun noComplexFailure(): Result<Nothing> =
        Result.failure(
            AppException(AppError.Validation(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX))
        )
}

/**
 * Sustituye el texto genérico de un 404 por el propio del endpoint consultado.
 *
 * El resto de errores (400 con mensaje del backend, 401/403, sin conexión) se
 * dejan tal cual los entrega [apiCall] vía `NetworkErrorMapper`.
 */
private fun <T> Result<T>.withNotFoundMessage(message: String): Result<T> = fold(
    onSuccess = { Result.success(it) },
    onFailure = { throwable ->
        val error = (throwable as? AppException)?.error
        if (error is AppError.NotFound) {
            Result.failure(AppException(AppError.NotFound(message)))
        } else {
            Result.failure(throwable)
        }
    }
)
