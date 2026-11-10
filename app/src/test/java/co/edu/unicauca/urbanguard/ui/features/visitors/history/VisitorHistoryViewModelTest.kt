package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.lifecycle.ViewModelStore
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryItem
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryPage
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class VisitorHistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val fixture = HistoryFixture()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() {
        fixture.close()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `solo ADMIN con conjunto inicia una consulta de pagina cero`() = runTest(dispatcher) {
        val vm = fixture.start(null)
        assertTrue(vm.uiState.value.results is HistoryResults.Unavailable)
        fixture.session.saveSession(adminSession(complexId = null))
        runCurrent()
        assertEquals(HistoryResults.NeedsComplex, vm.uiState.value.results)
        for (role in listOf(UserRole.RESIDENT, UserRole.GUARD, UserRole.SUPERADMIN, UserRole.USER)) {
            fixture.session.saveSession(adminSession().copy(role = role))
            runCurrent()
            assertTrue((vm.uiState.value.results as HistoryResults.Unavailable).error is AppError.Forbidden)
        }
        assertTrue(fixture.calls.isEmpty())

        fixture.session.saveSession(adminSession())
        runCurrent()
        assertEquals(HistoryResults.Loading, vm.uiState.value.results)
        assertEquals(HistoryQuery("a", HistoryDates(), 0, 20), fixture.calls.single().query)
        fixture.last.succeed(ids = listOf("entrada"))
        runCurrent()
        assertEquals(listOf("entrada"), vm.contentIds())
        assertEquals("Conjunto a", vm.uiState.value.complexName)
    }

    @Test
    fun `borrador no consulta y aplicar admite extremos independientes y fechas iguales`() = runTest(dispatcher) {
        val vm = fixture.start()
        val day = LocalDate.of(2026, 11, 9)
        runCurrent()
        vm.onAction(HistoryAction.EditFrom(day))
        runCurrent()
        assertEquals(1, fixture.calls.size)
        assertFalse(vm.uiState.value.applied.isFiltered)
        vm.onAction(HistoryAction.Apply)
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        assertEquals(2, fixture.calls.size)
        assertEquals(HistoryDates(from = day), fixture.last.query.dates)
        fixture.last.succeed()
        runCurrent()

        vm.onAction(HistoryAction.EditTo(day.minusDays(1)))
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        assertTrue(vm.uiState.value.invalidRange)
        assertEquals(2, fixture.calls.size)

        vm.onAction(HistoryAction.EditTo(day))
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        assertEquals(HistoryDates(day, day), fixture.last.query.dates)
        assertFalse(vm.uiState.value.invalidRange)
        vm.onAction(HistoryAction.EditFrom(null))
        assertEquals(HistoryDates(day, day), vm.uiState.value.applied)
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        assertEquals(HistoryQuery("a", HistoryDates(to = day), 0, 20), fixture.last.query)

        vm.onAction(HistoryAction.Clear)
        runCurrent()
        assertEquals(HistoryQuery("a", HistoryDates(), 0, 20), fixture.last.query)
        assertEquals(HistoryDates(), vm.uiState.value.draft)
        assertEquals(HistoryDates(), vm.uiState.value.applied)
    }

    @Test
    fun `fallo inicial reintenta pagina cero y distingue pagina vacia`() = runTest(dispatcher) {
        val vm = fixture.start()
        val error = AppError.NoConnection(AppError.NoConnection.MESSAGE)
        runCurrent()
        fixture.last.fail(error)
        runCurrent()
        assertEquals(HistoryResults.Failed(error), vm.uiState.value.results)
        vm.onAction(HistoryAction.Retry)
        runCurrent()
        assertEquals(HistoryQuery("a", HistoryDates(), 0, 20), fixture.last.query)
        fixture.last.succeed()
        runCurrent()
        assertTrue(vm.contentIds().isEmpty())
        assertNull(vm.content().nextPage)
    }

    @Test
    fun `pagina fallida mantiene lista y reintenta la misma pagina sin duplicados`() = runTest(dispatcher) {
        val vm = fixture.start()
        val day = LocalDate.of(2026, 11, 9)
        vm.onAction(HistoryAction.EditTo(day))
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        fixture.last.succeed(listOf("uno", "dos"), totalPages = 2)
        runCurrent()

        vm.onAction(HistoryAction.LoadNext)
        vm.onAction(HistoryAction.LoadNext)
        runCurrent()
        assertEquals(2, fixture.calls.size)
        val pageOne = HistoryQuery("a", HistoryDates(to = day), 1, 20)
        assertEquals(pageOne, fixture.last.query)
        assertEquals(HistoryOperation.LoadingNext, vm.content().operation)
        fixture.last.fail(AppError.Timeout(AppError.Timeout.MESSAGE))
        runCurrent()
        assertEquals(listOf("uno", "dos"), vm.contentIds())
        assertEquals(1, (vm.content().operation as HistoryOperation.NextFailed).page)

        vm.onAction(HistoryAction.Retry)
        runCurrent()
        assertEquals(pageOne, fixture.last.query)
        fixture.last.succeed(listOf("dos", "tres"), totalPages = 2)
        runCurrent()
        assertEquals(listOf("uno", "dos", "tres"), vm.contentIds())
        assertNull(vm.content().nextPage)
        vm.onAction(HistoryAction.LoadNext)
        runCurrent()
        assertEquals(3, fixture.calls.size)
    }

    @Test
    fun `actualizar conserva resultados y descarta una pagina que llega tarde`() = runTest(dispatcher) {
        val vm = fixture.start()
        runCurrent()
        fixture.last.succeed(listOf("vigente"), totalPages = 2)
        runCurrent()
        vm.onAction(HistoryAction.LoadNext)
        runCurrent()
        val obsolete = fixture.last
        vm.onAction(HistoryAction.Refresh)
        vm.onAction(HistoryAction.Refresh)
        vm.onAction(HistoryAction.LoadNext)
        runCurrent()
        assertEquals(3, fixture.calls.size)
        assertEquals(0, fixture.last.query.page)
        assertEquals(HistoryOperation.Refreshing, vm.content().operation)
        assertEquals(listOf("vigente"), vm.contentIds())

        fixture.last.fail(AppError.NoConnection(AppError.NoConnection.MESSAGE))
        obsolete.succeed(listOf("obsoleto"), totalPages = 2)
        runCurrent()
        assertTrue(vm.content().operation is HistoryOperation.RefreshFailed)
        assertEquals(listOf("vigente"), vm.contentIds())
        vm.onAction(HistoryAction.Retry)
        runCurrent()
        assertEquals(0, fixture.last.query.page)
        fixture.last.succeed(listOf("nuevo"))
        runCurrent()
        assertEquals(listOf("nuevo"), vm.contentIds())
    }

    @Test
    fun `respuestas de filtro y conjunto anteriores quedan descartadas`() = runTest(dispatcher) {
        val vm = fixture.start()
        runCurrent()
        val first = fixture.last
        vm.onAction(HistoryAction.EditFrom(LocalDate.of(2026, 11, 1)))
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        val filtered = fixture.last
        first.succeed(listOf("obsoleto por filtro"))
        runCurrent()
        assertEquals(HistoryResults.Loading, vm.uiState.value.results)

        fixture.session.saveSession(adminSession(complexId = "b"))
        runCurrent()
        assertEquals(HistoryQuery("b", HistoryDates(), 0, 20), fixture.last.query)
        assertEquals(HistoryDates(), vm.uiState.value.draft)
        fixture.last.succeed(listOf("conjunto b"))
        filtered.succeed(listOf("obsoleto por conjunto"))
        runCurrent()
        assertEquals(listOf("conjunto b"), vm.contentIds())
    }

    @Test
    fun `cambio de cuenta o perdida de sesion retira datos y respuestas pendientes`() = runTest(dispatcher) {
        val vm = fixture.start()
        runCurrent()
        fixture.last.succeed(listOf("primera cuenta"), totalPages = 2)
        runCurrent()
        vm.onAction(HistoryAction.LoadNext)
        runCurrent()
        val previous = fixture.last
        fixture.session.saveSession(adminSession(user = "otro"))
        runCurrent()
        assertEquals(HistoryResults.Loading, vm.uiState.value.results)
        fixture.last.succeed(listOf("segunda cuenta"))
        previous.succeed(listOf("página de la primera cuenta"), totalPages = 2)
        runCurrent()
        assertEquals(listOf("segunda cuenta"), vm.contentIds())
        fixture.session.clearSession()
        runCurrent()
        val error = (vm.uiState.value.results as HistoryResults.Unavailable).error
        assertTrue(error is AppError.SessionExpired)
        vm.onAction(HistoryAction.Retry)
        runCurrent()
        assertEquals(3, fixture.calls.size)
    }

    @Test
    fun `rol vigente de la asociacion bloquea acceso aunque la sesion indique ADMIN`() = runTest(dispatcher) {
        val vm = fixture.start()
        runCurrent()
        fixture.last.succeed(listOf("entrada"))
        runCurrent()
        fixture.session.setAssociations(listOf(ConjuntoUserAssociation("a", "Nombre nuevo", UserRole.RESIDENT)))
        runCurrent()
        assertTrue((vm.uiState.value.results as HistoryResults.Unavailable).error is AppError.Forbidden)
        assertEquals(1, fixture.calls.size)
    }

    @Test
    fun `perder acceso en otra pagina retira resultados y bloquea reintento`() = runTest(dispatcher) {
        val vm = fixture.start()
        val accessErrors = listOf(
            AppError.SessionExpired(AppError.SessionExpired.MESSAGE),
            AppError.Forbidden(AppError.Forbidden.MESSAGE),
            AppError.NotFound(AppError.NotFound.MESSAGE)
        )
        for ((index, error) in accessErrors.withIndex()) {
            fixture.session.saveSession(adminSession(user = "cuenta-$index"))
            runCurrent()
            fixture.last.succeed(listOf("entrada"), totalPages = 2)
            runCurrent()
            vm.onAction(HistoryAction.LoadNext)
            runCurrent()
            fixture.last.fail(error)
            runCurrent()
            assertEquals(HistoryResults.Unavailable(error), vm.uiState.value.results)
            val count = fixture.calls.size
            vm.onAction(HistoryAction.Retry)
            runCurrent()
            assertEquals(count, fixture.calls.size)
        }
    }

    @Test
    fun `400 de paginacion reinicia en cero y 400 de rango espera correccion`() = runTest(dispatcher) {
        val vm = fixture.start()
        runCurrent()
        fixture.last.succeed(listOf("primera"), totalPages = 2)
        runCurrent()
        vm.onAction(HistoryAction.LoadNext)
        runCurrent()
        fixture.last.fail(AppError.Validation("Paginación inválida"))
        runCurrent()
        assertEquals(listOf("primera"), vm.contentIds())
        vm.onAction(HistoryAction.Retry)
        runCurrent()
        assertEquals(0, fixture.last.query.page)

        val rangeError = AppError.Validation("Rango de fechas inválido")
        fixture.last.fail(rangeError)
        runCurrent()
        assertEquals(HistoryResults.Failed(rangeError), vm.uiState.value.results)
        vm.onAction(HistoryAction.Retry)
        runCurrent()
        assertEquals(3, fixture.calls.size)
        vm.onAction(HistoryAction.EditFrom(LocalDate.of(2026, 11, 1)))
        vm.onAction(HistoryAction.Apply)
        runCurrent()
        assertEquals(4, fixture.calls.size)
    }
}

private fun adminSession(complexId: String? = "a", user: String = "admin") =
    UserSession("jwt-$user", user, user, UserRole.ADMIN, conjuntoActivoId = complexId,
        conjuntoActivoNombre = complexId?.let { "Conjunto $it" })

private fun VisitorHistoryViewModel.content() = uiState.value.results as HistoryResults.Content
private fun VisitorHistoryViewModel.contentIds() = content().items.map { it.invitationId }

private data class HistoryQuery(val complexId: String, val dates: HistoryDates, val page: Int, val size: Int)

private class HistoryCall(val query: HistoryQuery, private val continuation: Continuation<Result<VisitorHistoryPage>>) {
    private var completed = false

    fun succeed(ids: List<String> = emptyList(), totalPages: Int = if (ids.isEmpty()) 0 else 1) {
        val items = ids.map { VisitorHistoryItem(it, "Creador", it, 1, Instant.EPOCH) }
        complete(Result.success(VisitorHistoryPage(items, query.page, query.size, items.size.toLong(), totalPages)))
    }

    fun fail(error: AppError) = complete(Result.failure(AppException(error)))

    fun cancelIfPending() {
        if (!completed) complete(Result.failure(kotlinx.coroutines.CancellationException()))
    }

    private fun complete(result: Result<VisitorHistoryPage>) {
        completed = true
        continuation.resume(result)
    }
}

private class HistoryFixture {
    val session = SessionManager(NoOpRolePersistence)
    val calls = mutableListOf<HistoryCall>()
    val last: HistoryCall get() = calls.last()
    private val store = ViewModelStore()

    fun start(user: UserSession? = adminSession()): VisitorHistoryViewModel {
        user?.let(session::saveSession)
        return VisitorHistoryViewModel(HistoryRepositoryFake(calls), session).also { store.put("history", it) }
    }

    fun close() {
        store.clear()
        calls.forEach(HistoryCall::cancelIfPending)
    }
}

private class HistoryRepositoryFake(private val calls: MutableList<HistoryCall>) : VisitorRepository {
    override suspend fun getVisitorHistory(
        complexId: String, from: LocalDate?, to: LocalDate?, page: Int, size: Int
    ): Result<VisitorHistoryPage> = suspendCoroutine { continuation ->
        // Models a transport response that arrives after cancellation.
        calls += HistoryCall(HistoryQuery(complexId, HistoryDates(from, to), page, size), continuation)
    }

    override suspend fun generateVisitorQr(visitorName: String, guestCount: Int, date: LocalDate, complexId: String) =
        error("Fuera del historial")
    override suspend fun getVisitorInvitations(complexId: String) = error("Fuera del historial")
    override suspend fun getInvitationCode(complexId: String, invitationId: String) = error("Fuera del historial")
    override fun setSelectedInvitation(invitation: VisitorInvitation?) = error("Fuera del historial")
    override fun selectedInvitation() = error("Fuera del historial")
    override suspend fun validateVisitorQr(code: String, complexId: String) = error("Fuera del historial")
    override fun lastGeneratedPass() = error("Fuera del historial")
}
