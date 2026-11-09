package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentType
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterOwnerTenantUseCaseTest {

    private val draft = OwnerTenantDraft(
        type = ResidentType.OWNER,
        nombres = "Ana",
        apellidos = "Pérez",
        documento = "123456789",
        email = "ana@example.com",
        telefono = "3001234567"
    )

    @Test
    fun `invoke sends complex unit and draft to the repository`() = runTest {
        val resident = OwnerTenant(
            id = "resident-1",
            unitId = "unit-1",
            unitIdentifier = "101",
            type = ResidentType.OWNER,
            fullName = "Ana Pérez"
        )
        val repository = FakeResidentialRepository(Result.success(resident))

        val result = RegisterOwnerTenantUseCase(repository)("complex-1", "unit-1", draft)

        assertTrue(result.isSuccess)
        assertEquals(resident, result.getOrNull())
        assertEquals("complex-1", repository.lastComplexId)
        assertEquals("unit-1", repository.lastUnitId)
        assertEquals(draft, repository.lastDraft)
    }

    @Test
    fun `invoke propagates repository failures untouched`() = runTest {
        val failure = Result.failure<OwnerTenant>(
            AppException(AppError.Conflict("La unidad ya tiene un propietario activo"))
        )
        val repository = FakeResidentialRepository(failure)

        val result = RegisterOwnerTenantUseCase(repository)("complex-1", "unit-1", draft)

        assertTrue(result.isFailure)
        assertEquals(failure.exceptionOrNull(), result.exceptionOrNull())
    }

    @Test
    fun `invoke rejects missing complex or unit without calling the repository`() = runTest {
        val repository = FakeResidentialRepository(
            Result.failure(AppException(AppError.Unexpected("No usado")))
        )
        val useCase = RegisterOwnerTenantUseCase(repository)

        val noComplex = useCase("", "unit-1", draft)
        val noUnit = useCase("complex-1", "", draft)

        assertTrue(noComplex.isFailure)
        assertTrue(noUnit.isFailure)
        assertTrue((noComplex.exceptionOrNull() as AppException).error is AppError.Validation)
        assertTrue((noUnit.exceptionOrNull() as AppException).error is AppError.Validation)
        assertEquals(0, repository.calls)
    }

    private class FakeResidentialRepository(
        private val registerResult: Result<OwnerTenant>
    ) : ResidentialRepository {
        var calls = 0
        var lastComplexId: String? = null
        var lastUnitId: String? = null
        var lastDraft: OwnerTenantDraft? = null

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
            Result.failure(AppException(AppError.Unexpected("No usado")))

        override suspend fun updateUnit(
            complexId: String,
            unitId: String,
            identifier: String
        ): Result<String> = Result.failure(AppException(AppError.Unexpected("No usado")))

        override suspend fun registerOwnerTenant(
            complexId: String,
            unitId: String,
            draft: OwnerTenantDraft
        ): Result<OwnerTenant> {
            calls++
            lastComplexId = complexId
            lastUnitId = unitId
            lastDraft = draft
            return registerResult
        }
    }
}
