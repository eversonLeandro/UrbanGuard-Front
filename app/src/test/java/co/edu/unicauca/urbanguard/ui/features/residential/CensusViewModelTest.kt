package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import co.edu.unicauca.urbanguard.domain.usecase.residential.GetCensusUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.RegisterUnitUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.SetOwnerTenantActiveUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.UpdateUnitUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CensusViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carga el censo del conjunto activo e incluye apartamentos vacios`() = runTest {
        val expected = listOf(
            ResidentialUnit(
                "unit-1",
                "101",
                listOf(ResidentialResident(userId = "user-1", fullName = "Ana Pérez"))
            ),
            ResidentialUnit("unit-2", "102", emptyList())
        )
        val repository = FakeResidentialRepository(Result.success(expected))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals("complex-1", repository.lastComplexId)
        assertEquals(expected, viewModel.uiState.value.units)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.error == null)
        assertTrue(viewModel.uiState.value.isAdmin)
    }

    @Test
    fun `cambia estado de residente usando membershipId cuando no hay ownerTenantId`() = runTest {
        val resident = ResidentialResident(
            membershipId = "membership-1",
            fullName = "Ana Pérez"
        )
        val unit = ResidentialUnit("unit-1", "101", listOf(resident))
        val repository = FakeResidentialRepository(Result.success(listOf(unit)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onChangeResidentStatus(unit.id, resident)
        advanceUntilIdle()

        assertEquals(1, repository.statusCalls)
        assertEquals("membership-1", repository.lastStatusResidentId)
        assertEquals(false, repository.lastStatusActive)
        assertEquals("Registro de Ana Pérez inactivado", viewModel.uiState.value.residentStatusSuccess)
    }

    @Test
    fun `muestra error y permite reintentar`() = runTest {
        val repository = FakeResidentialRepository(
            Result.failure(AppException(AppError.NoConnection("Sin conexión")))
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.error is AppError.NoConnection)

        repository.result = Result.success(emptyList())
        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals(2, repository.calls)
        assertTrue(viewModel.uiState.value.isEmpty)
        assertTrue(viewModel.uiState.value.error == null)
    }

    @Test
    fun `sin conjunto activo muestra validacion y no consulta`() = runTest {
        val repository = FakeResidentialRepository(Result.success(emptyList()))
        val viewModel = CensusViewModel(
            GetCensusUseCase(repository),
            RegisterUnitUseCase(repository),
            UpdateUnitUseCase(repository),
            SetOwnerTenantActiveUseCase(repository),
            SessionManager(NoOpRolePersistence)
        )

        advanceUntilIdle()

        assertEquals(0, repository.calls)
        assertTrue(viewModel.uiState.value.error is AppError.Validation)
    }

    @Test
    fun `conserva filtros y seleccion al abrir y cerrar el detalle`() = runTest {
        val repository = FakeResidentialRepository(
            Result.success(
                listOf(
                    ResidentialUnit(
                        "unit-1",
                        "101",
                        listOf(ResidentialResident(userId = "user-1", fullName = "Ana Pérez"))
                    )
                )
            )
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Ana")
        viewModel.onFilterChange(CensusUnitFilter.OCCUPIED)
        viewModel.onUnitSelected("unit-1")

        assertEquals("Ana", viewModel.uiState.value.searchQuery)
        assertEquals(CensusUnitFilter.OCCUPIED, viewModel.uiState.value.filter)
        assertEquals("unit-1", viewModel.uiState.value.selectedUnit?.id)

        viewModel.onDismissUnitDetail()
        assertTrue(viewModel.uiState.value.selectedUnit == null)
        assertEquals("Ana", viewModel.uiState.value.searchQuery)
        assertEquals(CensusUnitFilter.OCCUPIED, viewModel.uiState.value.filter)
    }

    @Test
    fun `actualiza el censo al volver a la pantalla sin duplicar la carga inicial`() = runTest {
        val unit = ResidentialUnit(
            "unit-1",
            "101",
            listOf(ResidentialResident(userId = "user-1", fullName = "Ana Pérez"))
        )
        val repository = FakeResidentialRepository(Result.success(listOf(unit)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Ana")
        viewModel.onFilterChange(CensusUnitFilter.OCCUPIED)
        viewModel.onUnitSelected(unit.id)
        viewModel.onScreenResumed()
        advanceUntilIdle()
        assertEquals(1, repository.calls)

        viewModel.onScreenResumed()
        advanceUntilIdle()
        assertEquals(2, repository.calls)
        assertEquals("Ana", viewModel.uiState.value.searchQuery)
        assertEquals(CensusUnitFilter.OCCUPIED, viewModel.uiState.value.filter)
        assertEquals(unit.id, viewModel.uiState.value.selectedUnit?.id)
    }

    @Test
    fun `registrar apartamento muestra exito y refresca el censo`() = runTest {
        val repository = FakeResidentialRepository(Result.success(emptyList()))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val newUnit = ResidentialUnit("unit-2", "2", emptyList())
        repository.result = Result.success(listOf(newUnit))
        repository.registerResult = Result.success(
            RegisteredResidentialUnit("unit-2", "2", 1)
        )

        viewModel.onRegisterUnit()
        advanceUntilIdle()

        assertEquals("complex-1", repository.lastRegisterComplexId)
        assertEquals(1, repository.registerCalls)
        assertEquals(2, repository.calls)
        assertEquals(listOf(newUnit), viewModel.uiState.value.units)
        assertEquals("Apartamento 2 agregado", viewModel.uiState.value.registrationSuccess)
        assertTrue(viewModel.uiState.value.registrationError == null)
        assertFalse(viewModel.uiState.value.isRegistering)
    }

    @Test
    fun `registrar apartamento muestra el error y conserva el censo`() = runTest {
        val unit = ResidentialUnit("unit-1", "1", emptyList())
        val repository = FakeResidentialRepository(Result.success(listOf(unit)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        repository.registerResult = Result.failure(
            AppException(AppError.Forbidden("Acceso denegado"))
        )

        viewModel.onRegisterUnit()
        advanceUntilIdle()

        assertEquals(1, repository.registerCalls)
        assertEquals(1, repository.calls)
        assertEquals(listOf(unit), viewModel.uiState.value.units)
        assertTrue(viewModel.uiState.value.registrationError is AppError.Forbidden)
        assertEquals(
            "Acceso denegado",
            viewModel.uiState.value.registrationError?.message
        )
        assertTrue(viewModel.uiState.value.registrationSuccess == null)
        assertFalse(viewModel.uiState.value.isRegistering)
    }

    @Test
    fun `registrar apartamento ignora dobles envios mientras esta en curso`() = runTest {
        val repository = FakeResidentialRepository(Result.success(emptyList()))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val gate = CompletableDeferred<Unit>()
        repository.registerGate = gate
        repository.result = Result.success(listOf(ResidentialUnit("unit-2", "2", emptyList())))
        repository.registerResult = Result.success(
            RegisteredResidentialUnit("unit-2", "2", 1)
        )

        viewModel.onRegisterUnit()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isRegistering)

        viewModel.onRegisterUnit()
        assertEquals(1, repository.registerCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRegistering)
    }

    @Test
    fun `editar apartamento muestra exito y refresca el censo`() = runTest {
        val original = ResidentialUnit("unit-1", "101", emptyList())
        val repository = FakeResidentialRepository(Result.success(listOf(original)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onUnitSelected(original.id)
        viewModel.onEditUnitDialogChange(true)
        val renamed = original.copy(identifier = "A-101")
        repository.result = Result.success(listOf(renamed))
        repository.updateResult = Result.success("A-101")

        viewModel.onUpdateUnit("  A-101  ")
        advanceUntilIdle()

        assertEquals("complex-1", repository.lastUpdateComplexId)
        assertEquals("unit-1", repository.lastUpdateUnitId)
        assertEquals("A-101", repository.lastUpdateIdentifier)
        assertEquals(1, repository.updateCalls)
        assertEquals(2, repository.calls)
        assertEquals(listOf(renamed), viewModel.uiState.value.units)
        assertEquals("Apartamento A-101 actualizado", viewModel.uiState.value.updateUnitSuccess)
        assertTrue(viewModel.uiState.value.updateUnitError == null)
        assertFalse(viewModel.uiState.value.isEditingUnit)
        assertFalse(viewModel.uiState.value.isUpdatingUnit)
    }

    @Test
    fun `editar apartamento con el mismo valor no llama al backend y cierra el dialogo`() = runTest {
        val unit = ResidentialUnit("unit-1", "101", emptyList())
        val repository = FakeResidentialRepository(Result.success(listOf(unit)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onUnitSelected(unit.id)
        viewModel.onEditUnitDialogChange(true)

        viewModel.onUpdateUnit("  101  ")
        advanceUntilIdle()

        assertEquals(0, repository.updateCalls)
        assertEquals(1, repository.calls)
        assertFalse(viewModel.uiState.value.isEditingUnit)
        assertTrue(viewModel.uiState.value.updateUnitError == null)
        assertTrue(viewModel.uiState.value.updateUnitSuccess == null)
    }

    @Test
    fun `editar apartamento muestra el error y mantiene el dialogo abierto`() = runTest {
        val unit = ResidentialUnit("unit-1", "101", emptyList())
        val repository = FakeResidentialRepository(Result.success(listOf(unit)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onUnitSelected(unit.id)
        viewModel.onEditUnitDialogChange(true)
        repository.updateResult = Result.failure(
            AppException(AppError.Forbidden("Acceso denegado"))
        )

        viewModel.onUpdateUnit("A-101")
        advanceUntilIdle()

        assertEquals(1, repository.updateCalls)
        assertEquals(1, repository.calls)
        assertEquals(listOf(unit), viewModel.uiState.value.units)
        assertTrue(viewModel.uiState.value.updateUnitError is AppError.Forbidden)
        assertEquals("Acceso denegado", viewModel.uiState.value.updateUnitError?.message)
        assertTrue(viewModel.uiState.value.isEditingUnit)
        assertFalse(viewModel.uiState.value.isUpdatingUnit)
        assertTrue(viewModel.uiState.value.updateUnitSuccess == null)
    }

    @Test
    fun `editar apartamento ignora dobles envios mientras esta en curso`() = runTest {
        val unit = ResidentialUnit("unit-1", "101", emptyList())
        val repository = FakeResidentialRepository(Result.success(listOf(unit)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onUnitSelected(unit.id)
        viewModel.onEditUnitDialogChange(true)
        val gate = CompletableDeferred<Unit>()
        repository.updateGate = gate
        repository.updateResult = Result.success("A-101")

        viewModel.onUpdateUnit("A-101")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isUpdatingUnit)

        viewModel.onUpdateUnit("B-101")
        assertEquals(1, repository.updateCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isUpdatingUnit)
    }

    private fun createViewModel(repository: FakeResidentialRepository): CensusViewModel {
        val sessionManager = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("token", "user-1", "Admin", UserRole.ADMIN))
            setAssociations(
                listOf(ConjuntoUserAssociation("complex-1", "Conjunto", UserRole.ADMIN))
            )
            setActiveConjunto("complex-1")
        }
        return CensusViewModel(
            GetCensusUseCase(repository),
            RegisterUnitUseCase(repository),
            UpdateUnitUseCase(repository),
            SetOwnerTenantActiveUseCase(repository),
            sessionManager
        )
    }
}

private class FakeResidentialRepository(
    var result: Result<List<ResidentialUnit>>
) : ResidentialRepository {
    var calls = 0
    var lastComplexId: String? = null
    var registerResult: Result<RegisteredResidentialUnit> =
        Result.success(RegisteredResidentialUnit("unit-new", "2", 1))
    var registerCalls = 0
    var lastRegisterComplexId: String? = null
    var registerGate: CompletableDeferred<Unit>? = null
    var updateResult: Result<String> = Result.success("unit-new")
    var updateCalls = 0
    var lastUpdateComplexId: String? = null
    var lastUpdateUnitId: String? = null
    var lastUpdateIdentifier: String? = null
    var updateGate: CompletableDeferred<Unit>? = null
    var statusCalls = 0
    var lastStatusResidentId: String? = null
    var lastStatusActive: Boolean? = null

    override suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>> {
        calls++
        lastComplexId = complexId
        return result
    }

    override suspend fun getCensus(
        complexId: String,
        status: String
    ): Result<List<ResidentialUnit>> {
        calls++
        lastComplexId = complexId
        return result
    }

    override suspend fun setOwnerTenantActive(
        complexId: String,
        unitId: String,
        residentId: String,
        active: Boolean
    ): Result<Unit> {
        statusCalls++
        lastStatusResidentId = residentId
        lastStatusActive = active
        return Result.success(Unit)
    }

    override suspend fun registerUnit(complexId: String): Result<RegisteredResidentialUnit> {
        registerCalls++
        lastRegisterComplexId = complexId
        registerGate?.await()
        return registerResult
    }

    override suspend fun updateUnit(
        complexId: String,
        unitId: String,
        identifier: String
    ): Result<String> {
        updateCalls++
        lastUpdateComplexId = complexId
        lastUpdateUnitId = unitId
        lastUpdateIdentifier = identifier
        updateGate?.await()
        return updateResult
    }

    override suspend fun registerOwnerTenant(
        complexId: String,
        unitId: String,
        draft: OwnerTenantDraft
    ): Result<OwnerTenant> = Result.failure(AppException(AppError.Unexpected("No usado")))
}
