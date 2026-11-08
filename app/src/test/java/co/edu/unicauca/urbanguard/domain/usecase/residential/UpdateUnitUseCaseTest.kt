package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import co.edu.unicauca.urbanguard.domain.validation.UnitIdentifierValidator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUnitUseCaseTest {

    @Test
    fun `invoke trims the identifier and returns the updated value`() = runTest {
        val repository = FakeResidentialRepository(Result.success("A-101"))
        val useCase = UpdateUnitUseCase(repository)

        val result = useCase("complex-1", "unit-1", "  A-101  ")

        assertTrue(result.isSuccess)
        assertEquals("A-101", result.getOrNull())
        assertEquals("complex-1", repository.lastComplexId)
        assertEquals("unit-1", repository.lastUnitId)
        assertEquals("A-101", repository.lastIdentifier)
    }

    @Test
    fun `invoke propagates repository failures untouched`() = runTest {
        val failure = Result.failure<String>(
            AppException(AppError.Conflict("La unidad ya existe en este conjunto"))
        )
        val useCase = UpdateUnitUseCase(FakeResidentialRepository(failure))

        val result = useCase("complex-1", "unit-1", "A-101")

        assertTrue(result.isFailure)
        assertEquals(failure.exceptionOrNull(), result.exceptionOrNull())
    }

    @Test
    fun `invoke rejects blank and invalid identifiers without calling the repository`() = runTest {
        val invalidIdentifiers = listOf(
            "",
            "   ",
            "A_101",
            "A.101",
            "A".repeat(UnitIdentifierValidator.MAX_LENGTH + 1)
        )

        invalidIdentifiers.forEach { identifier ->
            val repository = FakeResidentialRepository(Result.success("unused"))
            val result = UpdateUnitUseCase(repository)("complex-1", "unit-1", identifier)

            assertTrue(result.isFailure)
            assertTrue((result.exceptionOrNull() as AppException).error is AppError.Validation)
            assertEquals(0, repository.calls)
        }
    }

    @Test
    fun `invoke rejects missing complex or unit without calling the repository`() = runTest {
        val repository = FakeResidentialRepository(Result.success("unused"))
        val useCase = UpdateUnitUseCase(repository)

        assertTrue(useCase("", "unit-1", "A-101").isFailure)
        assertTrue(useCase("complex-1", "", "A-101").isFailure)
        assertEquals(0, repository.calls)
    }

    private class FakeResidentialRepository(
        private val updateResult: Result<String>
    ) : ResidentialRepository {
        var calls = 0
        var lastComplexId: String? = null
        var lastUnitId: String? = null
        var lastIdentifier: String? = null

        override suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>> =
            Result.success(emptyList())

        override suspend fun registerUnit(complexId: String): Result<RegisteredResidentialUnit> =
            Result.failure(AppException(AppError.Unexpected("No usado")))

        override suspend fun updateUnit(
            complexId: String,
            unitId: String,
            identifier: String
        ): Result<String> {
            calls++
            lastComplexId = complexId
            lastUnitId = unitId
            lastIdentifier = identifier
            return updateResult
        }

        override suspend fun registerOwnerTenant(
            complexId: String,
            unitId: String,
            draft: OwnerTenantDraft
        ): Result<OwnerTenant> = Result.failure(AppException(AppError.Unexpected("No usado")))
    }
}
