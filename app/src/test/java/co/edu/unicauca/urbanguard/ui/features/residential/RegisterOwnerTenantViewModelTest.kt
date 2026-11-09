package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentType
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import co.edu.unicauca.urbanguard.domain.usecase.residential.RegisterOwnerTenantUseCase
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
class RegisterOwnerTenantViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registra residente con datos recortados y muestra exito`() = runTest {
        val resident = OwnerTenant(
            id = "resident-1",
            unitId = "unit-1",
            unitIdentifier = "101",
            type = ResidentType.OWNER,
            fullName = "Ana Pérez"
        )
        val repository = FakeOwnerTenantRepository(Result.success(resident))
        val viewModel = createViewModel(repository)
        openAndFill(viewModel)

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("complex-1", repository.lastComplexId)
        assertEquals("unit-1", repository.lastUnitId)
        assertEquals("Ana", repository.lastDraft?.nombres)
        assertEquals("123456789", repository.lastDraft?.documento)
        assertEquals("Ana Pérez asignado al apartamento 101 como Propietario", state.successMessage)
        assertTrue(state.serverError == null)
        assertTrue(state.fieldErrors.isEmpty())
        assertFalse(state.isSaving)
    }

    @Test
    fun `valida todos los campos antes de llamar al repositorio`() = runTest {
        val repository = FakeOwnerTenantRepository(
            Result.failure(AppException(AppError.Unexpected("No usado")))
        )
        val viewModel = createViewModel(repository)
        viewModel.onOpen("unit-1")
        viewModel.onNombresChange(" ")
        viewModel.onApellidosChange("")
        viewModel.onDocumentoChange("12 34")
        viewModel.onEmailChange("no-es-email")
        viewModel.onTelefonoChange("abc")

        viewModel.onSubmit()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(5, errors.size)
        assertTrue(errors.containsKey(OwnerTenantField.NOMBRES))
        assertTrue(errors.containsKey(OwnerTenantField.APELLIDOS))
        assertTrue(errors.containsKey(OwnerTenantField.DOCUMENTO))
        assertTrue(errors.containsKey(OwnerTenantField.EMAIL))
        assertTrue(errors.containsKey(OwnerTenantField.TELEFONO))
        assertEquals(0, repository.calls)
    }

    @Test
    fun `muestra el error del backend y permite reintentar`() = runTest {
        val repository = FakeOwnerTenantRepository(
            Result.failure(
                AppException(AppError.Conflict("La unidad ya tiene un propietario activo"))
            )
        )
        val viewModel = createViewModel(repository)
        openAndFill(viewModel)

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.serverError is AppError.Conflict)
        assertEquals(
            "La unidad ya tiene un propietario activo",
            state.serverError?.message
        )
        assertTrue(state.successMessage == null)
        assertFalse(state.isSaving)
    }

    @Test
    fun `ignora dobles envios mientras esta guardando`() = runTest {
        val repository = FakeOwnerTenantRepository(Result.success(sampleResident()))
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val viewModel = createViewModel(repository)
        openAndFill(viewModel)

        viewModel.onSubmit()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaving)

        viewModel.onSubmit()
        assertEquals(1, repository.calls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `rechaza usuarios sin rol admin o sin conjunto activo`() = runTest {
        val repository = FakeOwnerTenantRepository(Result.success(sampleResident()))
        val nonAdmin = createViewModel(repository, role = UserRole.RESIDENT)
        openAndFill(nonAdmin)
        nonAdmin.onSubmit()
        advanceUntilIdle()

        assertTrue(nonAdmin.uiState.value.serverError is AppError.Forbidden)
        assertEquals(0, repository.calls)

        val noComplex = createViewModel(repository, activeComplex = false)
        openAndFill(noComplex)
        noComplex.onSubmit()
        advanceUntilIdle()

        assertTrue(noComplex.uiState.value.serverError is AppError.Validation)
        assertEquals(0, repository.calls)
    }

    private fun openAndFill(viewModel: RegisterOwnerTenantViewModel) {
        viewModel.onOpen("unit-1")
        viewModel.onTypeChange(ResidentType.OWNER)
        viewModel.onNombresChange("  Ana  ")
        viewModel.onApellidosChange("Pérez")
        viewModel.onDocumentoChange("123456789")
        viewModel.onEmailChange("ana@example.com")
        viewModel.onTelefonoChange("3001234567")
    }

    private fun createViewModel(
        repository: FakeOwnerTenantRepository,
        role: UserRole = UserRole.ADMIN,
        activeComplex: Boolean = true
    ): RegisterOwnerTenantViewModel {
        val sessionManager = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("token", "user-1", "Admin", role))
            setAssociations(
                listOf(ConjuntoUserAssociation("complex-1", "Conjunto", role))
            )
            if (activeComplex) {
                setActiveConjunto("complex-1")
            }
        }
        return RegisterOwnerTenantViewModel(
            RegisterOwnerTenantUseCase(repository),
            sessionManager
        )
    }

    private fun sampleResident() = OwnerTenant(
        id = "resident-1",
        unitId = "unit-1",
        unitIdentifier = "101",
        type = ResidentType.OWNER,
        fullName = "Ana Pérez"
    )
}

private class FakeOwnerTenantRepository(
    private val registerResult: Result<OwnerTenant>
) : ResidentialRepository {
    var calls = 0
    var lastComplexId: String? = null
    var lastUnitId: String? = null
    var lastDraft: OwnerTenantDraft? = null
    var gate: CompletableDeferred<Unit>? = null

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
        gate?.await()
        return registerResult
    }
}
