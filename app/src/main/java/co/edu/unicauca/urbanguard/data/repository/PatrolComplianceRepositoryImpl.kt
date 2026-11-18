package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolComplianceApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCompliance
import co.edu.unicauca.urbanguard.domain.repository.PatrolComplianceRepository
import javax.inject.Inject

/**
 * Detalle de cumplimiento de una ronda (nivel 3 del historial, HU-032).
 *
 * No hay cálculo en el frontend: llama al backend y traduce el contrato con el
 * mismo criterio del resto del módulo (fallar ante una respuesta inválida).
 */
class PatrolComplianceRepositoryImpl @Inject constructor(
    private val api: PatrolComplianceApi
) : PatrolComplianceRepository {

    override suspend fun getRoundCompliance(
        complexId: String,
        occurrenceId: String
    ): Result<PatrolRoundCompliance> {
        if (complexId.isBlank()) return noComplexFailure()
        return apiCall {
            api.getRoundDetail(complexId = complexId, occurrenceId = occurrenceId).toDomain()
        }.withNotFoundMessage(PatrolHistoryConstants.MSG_ROUND_NOT_FOUND)
    }

    private fun noComplexFailure(): Result<Nothing> =
        Result.failure(
            AppException(AppError.Validation(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX))
        )
}

/**
 * Sustituye el texto genérico de un 404 por el propio del endpoint consultado,
 * igual que en [PatrolShiftRepositoryImpl].
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
