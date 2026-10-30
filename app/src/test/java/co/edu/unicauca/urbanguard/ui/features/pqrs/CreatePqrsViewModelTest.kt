package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.SavedStateHandle
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDraft
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ResidentPqrsRepository
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.CreatePqrsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePqrsViewModelTest {
    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `valida campos vacios y limites sin llamar a red`() = runTest {
        val repo = FakeResidentRepo()
        val vm = viewModel(repo)
        vm.submit()
        assertEquals("Selecciona un tipo de PQRS.", vm.uiState.value.typeError)
        assertEquals("Escribe el asunto.", vm.uiState.value.subjectError)
        assertEquals("Escribe la descripción.", vm.uiState.value.descriptionError)
        vm.onTypeChanged(PqrsType.PETITION)
        vm.onSubjectChanged("x".repeat(121))
        vm.onDescriptionChanged("x".repeat(2001))
        vm.submit()
        assertTrue(vm.uiState.value.subjectError!!.contains("120"))
        assertTrue(vm.uiState.value.descriptionError!!.contains("2000"))
        assertTrue(repo.creates.isEmpty())
    }

    @Test fun `envia datos normalizados con llave valida y evita doble toque`() = runTest {
        val repo = FakeResidentRepo()
        val vm = viewModel(repo)
        fill(vm)
        vm.submit()
        vm.submit()
        assertTrue(vm.uiState.value.isSubmitting)
        advanceUntilIdle()
        assertEquals(1, repo.creates.size)
        assertEquals("Asunto", repo.creates.single().draft.subject)
        assertEquals("Descripción", repo.creates.single().draft.description)
        assertEquals("complex-1", repo.creates.single().complexId)
        assertTrue(repo.creates.single().key.length in 1..128)
        assertFalse(vm.uiState.value.isSubmitting)
        assertEquals("pqrs-1", vm.createdIds.first())
    }

    @Test fun `error transitorio conserva llave al reintentar`() = runTest {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.NoConnection(AppError.NoConnection.MESSAGE)))
        }
        val vm = viewModel(repo)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.submitError!!.contains("conexión"))
        repo.nextResult = Result.success(detail())
        vm.submit()
        advanceUntilIdle()
        assertEquals(repo.creates[0].key, repo.creates[1].key)
    }

    @Test fun `cambio de payload exige intento explicito con llave nueva`() = runTest {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.ServerError(AppError.ServerError.MESSAGE)))
        }
        val vm = viewModel(repo)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        vm.onSubjectChanged("Otra solicitud")
        vm.submit()
        assertTrue(vm.uiState.value.requiresNewAttempt)
        assertEquals(1, repo.creates.size)
        vm.startNewAttempt()
        advanceUntilIdle()
        assertEquals(2, repo.creates.size)
        assertNotEquals(repo.creates[0].key, repo.creates[1].key)
    }

    @Test fun `conflicto exige intento nuevo y conserva formulario`() = runTest {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.Conflict(AppError.Conflict.MESSAGE)))
        }
        val vm = viewModel(repo)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        assertEquals(CreatePqrsViewModel.CONFLICT_MESSAGE, vm.uiState.value.submitError)
        assertTrue(vm.uiState.value.requiresNewAttempt)
        assertEquals(" Asunto ", vm.uiState.value.subject)
        vm.submit()
        assertEquals(1, repo.creates.size)
    }

    @Test fun `recreacion conserva formulario e intento pendiente`() = runTest {
        val handle = SavedStateHandle()
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.ServerError(AppError.ServerError.MESSAGE)))
        }
        val session = activeSession()
        val original = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), session)
        fill(original)
        original.submit()
        advanceUntilIdle()
        val recreated = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), session)
        assertEquals(" Asunto ", recreated.uiState.value.subject)
        recreated.submit()
        advanceUntilIdle()
        assertEquals(repo.creates[0].key, repo.creates[1].key)
    }

    @Test fun `sin conjunto activo impide envio`() = runTest {
        val repo = FakeResidentRepo()
        val vm = viewModel(repo, activeSession(""))
        fill(vm)
        vm.submit()
        assertTrue(vm.uiState.value.submitError!!.contains("conjunto activo"))
        assertTrue(repo.creates.isEmpty())
    }

    @Test fun `rol distinto de residente impide crear`() = runTest {
        val repo = FakeResidentRepo()
        val session = activeSession()
        session.saveSession(requireNotNull(session.session.value).copy(role = UserRole.ADMIN))
        val vm = viewModel(repo, session)
        fill(vm)
        vm.submit()
        assertEquals(AppError.Forbidden.MESSAGE, vm.uiState.value.submitError)
        assertTrue(repo.creates.isEmpty())
    }

    @Test fun `cambio de conjunto antes del reintento exige llave nueva`() = runTest {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.ServerError(AppError.ServerError.MESSAGE)))
        }
        val session = activeSession()
        val vm = viewModel(repo, session)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        session.saveSession(requireNotNull(session.session.value).copy(conjuntoActivoId = "complex-2"))
        vm.submit()
        assertTrue(vm.uiState.value.requiresNewAttempt)
        assertEquals(1, repo.creates.size)
        vm.startNewAttempt()
        advanceUntilIdle()
        assertEquals("complex-2", repo.creates.last().complexId)
        assertNotEquals(repo.creates.first().key, repo.creates.last().key)
    }

    @Test fun `cambio de conjunto durante POST evita abrir detalle ajeno`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeResidentRepo().apply { createGate = gate }
        val session = activeSession()
        val handle = SavedStateHandle()
        val vm = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), session)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        session.saveSession(requireNotNull(session.session.value).copy(conjuntoActivoId = "complex-2"))
        gate.complete(Unit)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.completedElsewhere)
        assertEquals(CreatePqrsViewModel.COMPLETED_ELSEWHERE_MESSAGE, vm.uiState.value.submitError)
        vm.submit()
        assertEquals(1, repo.creates.size)
        val recreated = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), session)
        assertTrue(recreated.uiState.value.completedElsewhere)
        recreated.submit()
        assertEquals(1, repo.creates.size)
    }

    @Test fun `cambio de usuario exige nueva llave antes de reintentar`() = runTest {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.ServerError(AppError.ServerError.MESSAGE)))
        }
        val session = activeSession()
        val vm = viewModel(repo, session)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        session.saveSession(requireNotNull(session.session.value).copy(userId = "resident-2"))
        vm.submit()
        assertTrue(vm.uiState.value.requiresNewAttempt)
        assertEquals(1, repo.creates.size)
    }

    @Test fun `cambio de identidad limpia borrador visible y guardado`() = runTest {
        val session = activeSession()
        val handle = SavedStateHandle()
        val repo = FakeResidentRepo()
        val vm = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), session)
        fill(vm)
        advanceUntilIdle()
        session.saveSession(requireNotNull(session.session.value).copy(userId = "resident-2"))
        advanceUntilIdle()
        assertEquals("", vm.uiState.value.subject)
        assertEquals("", vm.uiState.value.description)
        val recreated = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), session)
        assertEquals("", recreated.uiState.value.subject)
    }

    @Test fun `restauracion oculta borrador hasta verificar el mismo usuario`() = runTest {
        val ownerSession = activeSession()
        val handle = SavedStateHandle()
        val repo = FakeResidentRepo()
        val original = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), ownerSession)
        fill(original)
        val restoringSession = SessionManager(NoOpRolePersistence)
        val restored = CreatePqrsViewModel(handle, CreatePqrsUseCase(repo), restoringSession)
        assertEquals("", restored.uiState.value.subject)
        restoringSession.saveSession(requireNotNull(ownerSession.session.value))
        advanceUntilIdle()
        assertEquals(" Asunto ", restored.uiState.value.subject)
    }

    @Test fun `respuesta tardia de usuario anterior no borra intento nuevo`() = runTest {
        val oldGate = CompletableDeferred<Unit>()
        val repo = FakeResidentRepo().apply { createGate = oldGate }
        val session = activeSession()
        val vm = viewModel(repo, session)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        session.saveSession(requireNotNull(session.session.value).copy(userId = "resident-2"))
        advanceUntilIdle()
        repo.createGate = null
        repo.nextResult = Result.failure(AppException(AppError.NoConnection(AppError.NoConnection.MESSAGE)))
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        val newKey = repo.creates.last().key
        oldGate.complete(Unit)
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertEquals(newKey, repo.creates.last().key)
        assertEquals(3, repo.creates.size)
    }

    @Test fun `respuesta antigua tras logout y login del mismo usuario no navega`() = runTest {
        val oldGate = CompletableDeferred<Unit>()
        val repo = FakeResidentRepo().apply { createGate = oldGate }
        val session = activeSession()
        val user = requireNotNull(session.session.value)
        val vm = viewModel(repo, session)
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        session.clearSession()
        advanceUntilIdle()
        session.saveSession(user)
        advanceUntilIdle()
        repo.createGate = null
        repo.nextResult = Result.failure(AppException(AppError.NoConnection(AppError.NoConnection.MESSAGE)))
        fill(vm)
        vm.submit()
        advanceUntilIdle()
        val newKey = repo.creates.last().key
        val error = vm.uiState.value.submitError
        oldGate.complete(Unit)
        advanceUntilIdle()
        assertEquals(error, vm.uiState.value.submitError)
        vm.submit()
        advanceUntilIdle()
        assertEquals(newKey, repo.creates.last().key)
    }

    private fun viewModel(
        repo: FakeResidentRepo,
        session: SessionManager = activeSession()
    ) = CreatePqrsViewModel(SavedStateHandle(), CreatePqrsUseCase(repo), session)

    private fun fill(vm: CreatePqrsViewModel) {
        vm.onTypeChanged(PqrsType.PETITION)
        vm.onSubjectChanged(" Asunto ")
        vm.onDescriptionChanged(" Descripción ")
    }
}

internal fun activeSession(complexId: String = "complex-1") = SessionManager(NoOpRolePersistence).apply {
    saveSession(
        UserSession(
            token = "test", userId = "resident-1", name = "Residente",
            role = UserRole.RESIDENT, conjuntoActivoId = complexId
        )
    )
}

internal fun detail(id: String = "pqrs-1") = ResidentPqrsDetail(
    id, PqrsType.PETITION, "Asunto", "Descripción", PqrsStatus.SUBMITTED,
    "2026-10-30T12:00:00Z"
)

internal class FakeResidentRepo : ResidentPqrsRepository {
    data class CreateCall(val complexId: String, val key: String, val draft: ResidentPqrsDraft)
    val creates = mutableListOf<CreateCall>()
    val queries = mutableListOf<Triple<String, PqrsStatus?, PqrsType?>>()
    var nextResult: Result<ResidentPqrsDetail> = Result.success(detail())
    var mine: Result<List<ResidentPqrsSummary>> = Result.success(emptyList())
    var unfilteredMine: Result<List<ResidentPqrsSummary>>? = null
    var ownDetail: Result<ResidentPqrsDetail> = Result.success(detail())
    var createGate: CompletableDeferred<Unit>? = null
    var detailGate: CompletableDeferred<Unit>? = null
    override suspend fun create(
        complexId: String,
        idempotencyKey: String,
        draft: ResidentPqrsDraft
    ): Result<ResidentPqrsDetail> {
        creates += CreateCall(complexId, idempotencyKey, draft)
        val result = nextResult
        createGate?.await()
        return result
    }
    override suspend fun getMine(
        complexId: String,
        status: PqrsStatus?,
        type: PqrsType?
    ): Result<List<ResidentPqrsSummary>> {
        queries += Triple(complexId, status, type)
        return if (status == null && type == null) unfilteredMine ?: mine else mine
    }
    override suspend fun getOwnDetail(complexId: String, pqrsId: String): Result<ResidentPqrsDetail> {
        detailGate?.await()
        return ownDetail
    }
}
