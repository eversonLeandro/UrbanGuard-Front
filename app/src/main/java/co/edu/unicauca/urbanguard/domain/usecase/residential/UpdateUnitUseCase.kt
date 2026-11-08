package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import co.edu.unicauca.urbanguard.domain.validation.UnitIdentifierValidator
import javax.inject.Inject

class UpdateUnitUseCase @Inject constructor(
    private val repository: ResidentialRepository
) {
    suspend operator fun invoke(
        complexId: String,
        unitId: String,
        identifier: String
    ): Result<String> {
        if (complexId.isBlank()) {
            return validationFailure(NO_ACTIVE_COMPLEX_MESSAGE)
        }
        if (unitId.isBlank()) {
            return validationFailure(NO_UNIT_MESSAGE)
        }

        val trimmedIdentifier = identifier.trim()
        UnitIdentifierValidator.validate(trimmedIdentifier)?.let { message ->
            return validationFailure(message)
        }

        return repository.updateUnit(complexId, unitId, trimmedIdentifier)
    }

    private fun <T> validationFailure(message: String): Result<T> =
        Result.failure(AppException(AppError.Validation(message)))

    private companion object {
        const val NO_ACTIVE_COMPLEX_MESSAGE = "No hay un conjunto activo seleccionado."
        const val NO_UNIT_MESSAGE = "No se seleccionó un apartamento."
    }
}
