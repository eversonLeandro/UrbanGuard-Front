package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUnitUseCaseTest {

    @Test
    fun `invoke returns the created unit on success`() = runTest {
        val created = RegisteredResidentialUnit("unit-1", "101", 1)
        val useCase = RegisterUnitUseCase(FakeResidentialRepository(Result.success(created)))

        val result = useCase("complex-1")

        assertTrue(result.isSuccess)
        assertEquals(created, result.getOrNull())
    }

    @Test
    fun `invoke propagates repository failures untouched`() = runTest {
        val failure = Result.failure<RegisteredResidentialUnit>(
            AppException(AppError.Forbidden("Acceso denegado"))
        )
        val useCase = RegisterUnitUseCase(FakeResidentialRepository(failure))

        val result = useCase("complex-1")

        assertTrue(result.isFailure)
        assertEquals(failure.exceptionOrNull(), result.exceptionOrNull())
    }

    private class FakeResidentialRepository(
        private val registerResult: Result<RegisteredResidentialUnit>
    ) : ResidentialRepository {
        override suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>> =
            Result.success(emptyList())

        override suspend fun getCensus(
            complexId: String,
            status: String
        ): Result<List<ResidentialUnit>> = Result.success(emptyList())

        override suspend fun setOwnerTenantActive(
            complexId: String,
            unitId: String,
            residentId: String,
            active: Boolean
        ): Result<Unit> = Result.success(Unit)

        override suspend fun registerUnit(complexId: String): Result<RegisteredResidentialUnit> =
            registerResult

        override suspend fun updateUnit(
            complexId: String,
            unitId: String,
            identifier: String
        ): Result<String> = Result.failure(AppException(AppError.Unexpected("No usado")))

        override suspend fun registerOwnerTenant(
            complexId: String,
            unitId: String,
            draft: OwnerTenantDraft
        ): Result<OwnerTenant> = Result.failure(AppException(AppError.Unexpected("No usado")))
    }
}
