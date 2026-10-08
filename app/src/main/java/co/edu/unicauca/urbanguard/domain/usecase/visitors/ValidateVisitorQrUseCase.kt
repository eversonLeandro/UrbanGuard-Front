package co.edu.unicauca.urbanguard.domain.usecase.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorValidationResult
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import javax.inject.Inject

/**
 * Valida un pase QR de visitante (flujo guardia, un solo uso).
 *
 * Delega en el repositorio, que ya mapea el `reason` del backend a un
 * [co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure] tipado.
 */
class ValidateVisitorQrUseCase @Inject constructor(
    private val repository: VisitorRepository
) {
    suspend operator fun invoke(
        code: String,
        complexId: String
    ): Result<VisitorValidationResult> {
        return repository.validateVisitorQr(code, complexId)
    }
}
