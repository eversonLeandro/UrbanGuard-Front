package co.edu.unicauca.urbanguard.ui.features.admin

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.domain.model.MembershipRole
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import co.edu.unicauca.urbanguard.domain.usecase.guards.RegisterGuardUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * El ViewModel es el único sitio donde se decide qué se envía al backend, así
 * que estos tests fijan las tres fronteras: lo que se bloquea en el cliente sin
 * llamar al servidor, lo que se traduce desde un `AppError` ya resuelto, y el
 * hecho de que un doble toque no dispare dos altas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterGuardViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `un alta valida deja el vigilante creado y limpia el estado de envio`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CREATED, state.createdGuard)
        assertNull(state.errorMessage)
        assertTrue("El envío ya no debe seguir en curso", !state.isSubmitting)
        assertEquals(COMPLEX_ID, repo.lastComplexId)
    }

    @Test
    fun `los valores se recortan antes de enviarse`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        viewModel.onEmailChange("  guardia@urbanguard.com  ")
        viewModel.onNombresChange("  Carlos  ")
        viewModel.onApellidosChange("  Pérez  ")
        viewModel.onTelefonoChange("  3001234567  ")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals("guardia@urbanguard.com", repo.lastEmail)
        assertEquals("Carlos", repo.lastNombres)
        assertEquals("Pérez", repo.lastApellidos)
        assertEquals("3001234567", repo.lastTelefono)
    }

    @Test
    fun `un email mal formado se marca en su campo y no se llama al servidor`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onEmailChange("correo-sin-arroba")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "El correo no tiene un formato válido",
            viewModel.uiState.value.emailError
        )
        assertEquals("No debe haber chamado al repositorio", 0, repo.calls)
    }

    @Test
    fun `un telefono con letras se marca en su campo`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onTelefonoChange("300-abc-4567")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "El teléfono solo admite dígitos, +, (), - y espacios",
            viewModel.uiState.value.telefonoError
        )
        assertEquals(0, repo.calls)
    }

    @Test
    fun `los cuatro campos en blanco se reportan a la vez`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.nombresError)
        assertNotNull(state.apellidosError)
        assertNotNull(state.telefonoError)
        assertEquals(0, repo.calls)
    }

    @Test
    fun `sin conjunto en la sesion se avisa y no se llama al servidor`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo, sessionManager = SessionManager(NoOpRolePersistence))

        fillValidForm(viewModel)
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "No hay un conjunto seleccionado. Elige uno e inténtalo de nuevo.",
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, repo.calls)
    }

    @Test
    fun `el doble toque en el boton solo dispara un alta`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        // Sin advanceUntilIdle entre medias: la segunda llamada ocurre mientras la
        // primera sigue en vuelo, que es justo el doble toque real del usuario.
        viewModel.onSubmit()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, repo.calls)
    }

    @Test
    fun `un 403 se muestra como banner con el mensaje del backend`() = runTest {
        val repo = FakeGuardRepository(
            error = AppException(AppError.Forbidden("Acceso denegado"))
        )
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Acceso denegado", state.errorMessage)
        assertNull(state.createdGuard)
    }

    @Test
    fun `un 409 por vigilante ya existente conserva el mensaje del backend`() = runTest {
        val repo = FakeGuardRepository(
            error = AppException(
                AppError.Conflict("El vigilante ya es personal de este conjunto")
            )
        )
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "El vigilante ya es personal de este conjunto",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `editar un campo limpia su error y el banner`() = runTest {
        val repo = FakeGuardRepository(
            error = AppException(AppError.Conflict("El vigilante ya es personal de este conjunto"))
        )
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onEmailChange("otro@urbanguard.com")

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNull(state.emailError)
    }

    @Test
    fun `descartar el vigilante creado deja la pantalla lista para otro alta`() = runTest {
        val repo = FakeGuardRepository(created = CREATED)
        val viewModel = createViewModel(repo)

        fillValidForm(viewModel)
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onCreatedGuardHandled()

        assertNull(viewModel.uiState.value.createdGuard)
    }

    private fun fillValidForm(viewModel: RegisterGuardViewModel) {
        viewModel.onEmailChange("guardia@urbanguard.com")
        viewModel.onNombresChange("Carlos")
        viewModel.onApellidosChange("Pérez")
        viewModel.onTelefonoChange("3001234567")
    }

    private fun createViewModel(
        repository: GuardRepository,
        sessionManager: SessionManager = sessionWithComplex()
    ) = RegisterGuardViewModel(
        registerGuardUseCase = RegisterGuardUseCase(repository),
        sessionManager = sessionManager
    )

    private fun sessionWithComplex(): SessionManager {
        val manager = SessionManager(NoOpRolePersistence)
        manager.saveSession(
            UserSession(
                token = "token",
                userId = "admin-1",
                name = "Admin",
                role = UserRole.ADMIN,
                conjuntosAsociados = listOf(
                    ConjuntoUserAssociation(
                        conjuntoId = COMPLEX_ID,
                        nombre = "Residencial Arboledas",
                        roleEnConjunto = UserRole.ADMIN
                    )
                ),
                conjuntoActivoId = COMPLEX_ID
            )
        )
        return manager
    }

    private class FakeGuardRepository(
        private val created: CreatedGuard? = null,
        private val error: Exception? = null
    ) : GuardRepository {

        var calls: Int = 0
            private set
        var lastComplexId: String? = null
            private set
        var lastEmail: String? = null
            private set
        var lastNombres: String? = null
            private set
        var lastApellidos: String? = null
            private set
        var lastTelefono: String? = null
            private set

        override suspend fun registerGuard(
            complexId: String,
            email: String,
            nombres: String,
            apellidos: String,
            telefono: String
        ): Result<CreatedGuard> {
            calls++
            lastComplexId = complexId
            lastEmail = email
            lastNombres = nombres
            lastApellidos = apellidos
            lastTelefono = telefono
            return if (error != null) Result.failure(error) else Result.success(created!!)
        }

        override suspend fun getGuards(
            complexId: String,
            status: String
        ): Result<List<co.edu.unicauca.urbanguard.domain.model.GuardMembership>> =
            Result.success(emptyList())

        override suspend fun setGuardActive(
            complexId: String,
            membershipId: String,
            active: Boolean
        ): Result<Unit> = Result.success(Unit)
    }

    private companion object {
        const val COMPLEX_ID = "6f1c9a70-0000-4000-8000-000000000001"

        val CREATED = CreatedGuard(
            membershipId = "6f1c9a70-0000-4000-8000-000000000002",
            userId = "6f1c9a70-0000-4000-8000-000000000003",
            email = "guardia@urbanguard.com",
            nombres = "Carlos",
            role = MembershipRole.GUARD
        )
    }
}
