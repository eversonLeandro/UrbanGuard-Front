package co.edu.unicauca.urbanguard.ui.features.notifications

import co.edu.unicauca.urbanguard.domain.model.NotificationItem
import co.edu.unicauca.urbanguard.domain.model.NotificationPage
import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import co.edu.unicauca.urbanguard.domain.usecase.notifications.GetNotificationsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.notifications.MarkAllNotificationsReadUseCase
import co.edu.unicauca.urbanguard.domain.usecase.notifications.MarkNotificationReadUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * El ViewModel de la bandeja orquesta: carga paginada, filtro de no leídas,
 * marcado optimista con reversión y sincronización de la insignia compartida.
 * Se prueba con un repositorio falso, sin backend ni UI.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationInboxViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `la carga inicial expone la primera pagina`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.success(page(listOf(item("n1"), item("n2", read = true)))) }
        }

        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(2, state.items.size)
        assertFalse(state.isEmpty)
        assertEquals(listOf(0 to false), repo.requestedPages)
    }

    @Test
    fun `un fallo de carga expone el mensaje de error`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.failure(RuntimeException("sin conexion")) }
        }

        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("sin conexion", state.errorMessage)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `el filtro de no leidas reinicia el listado con el filtro aplicado`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.success(page(listOf(item("n1")))) }
        }
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onToggleUnreadOnly()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.unreadOnly)
        assertEquals(0 to true, repo.requestedPages.last())
    }

    @Test
    fun `cargar mas anexa la siguiente pagina`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { page, _ ->
                if (page == 0) {
                    Result.success(page(listOf(item("n1")), page = 0, totalPages = 2))
                } else {
                    Result.success(page(listOf(item("n2")), page = 1, totalPages = 2))
                }
            }
        }
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(listOf("n1", "n2"), viewModel.uiState.value.items.map { it.id })
        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `marcar un aviso leido actualiza el estado y decrementa la insignia`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.success(page(listOf(item("n1")))) }
        }
        val badge = NotificationBadgeStore().apply { set(3) }
        val viewModel = createViewModel(repo, badge)
        advanceUntilIdle()

        viewModel.onNotificationClick(item("n1"))
        advanceUntilIdle()

        assertEquals(listOf("n1"), repo.markedIds)
        assertNotNull(viewModel.uiState.value.items.first().readAt)
        assertEquals(2, badge.unreadCount.value)
    }

    @Test
    fun `el marcado fallido revierte el estado y muestra un banner`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.success(page(listOf(item("n1")))) }
            markReadResult = Result.failure(RuntimeException("no se pudo"))
        }
        val badge = NotificationBadgeStore().apply { set(3) }
        val viewModel = createViewModel(repo, badge)
        advanceUntilIdle()

        viewModel.onNotificationClick(item("n1"))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.items.first().readAt)
        assertEquals("no se pudo", viewModel.uiState.value.actionError)
        assertEquals(3, badge.unreadCount.value)
    }

    @Test
    fun `un aviso con deepLink emite el evento de navegacion`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.success(page(listOf(item("n1", read = true)))) }
        }
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        val received = async { viewModel.events.first() }
        viewModel.onNotificationClick(item("n1", read = true, deepLink = "urbanguard://requests/9"))
        advanceUntilIdle()

        assertEquals(NotificationInboxEvent.OpenDeepLink("urbanguard://requests/9"), received.await())
    }

    @Test
    fun `marcar todas marca los avisos y limpia la insignia`() = runTest {
        val repo = FakeNotificationInboxRepository().apply {
            responder = { _, _ -> Result.success(page(listOf(item("n1"), item("n2")))) }
        }
        val badge = NotificationBadgeStore().apply { set(2) }
        val viewModel = createViewModel(repo, badge)
        advanceUntilIdle()

        viewModel.onMarkAllRead()
        advanceUntilIdle()

        assertTrue(repo.markAllCalled)
        assertTrue(viewModel.uiState.value.items.all { it.isRead })
        assertEquals(0, badge.unreadCount.value)
    }

    private fun createViewModel(
        repo: FakeNotificationInboxRepository,
        badge: NotificationBadgeStore = NotificationBadgeStore()
    ) = NotificationInboxViewModel(
        GetNotificationsUseCase(repo),
        MarkNotificationReadUseCase(repo),
        MarkAllNotificationsReadUseCase(repo),
        badge
    )

    private fun item(id: String, read: Boolean = false, deepLink: String? = null) = NotificationItem(
        id = id,
        title = "Titulo",
        body = "Cuerpo",
        deepLink = deepLink,
        channel = "requests",
        priority = "HIGH",
        conjuntoId = "conjunto",
        createdAt = "2026-10-14T12:00:00Z",
        readAt = if (read) "2026-10-14T12:05:00Z" else null
    )

    private fun page(
        items: List<NotificationItem>,
        page: Int = 0,
        totalPages: Int = 1
    ) = NotificationPage(items, page, 20, items.size.toLong(), totalPages)

    private class FakeNotificationInboxRepository : NotificationInboxRepository {

        val requestedPages = mutableListOf<Pair<Int, Boolean>>()
        var responder: (Int, Boolean) -> Result<NotificationPage> = { _, _ ->
            Result.success(NotificationPage(emptyList(), 0, 20, 0, 0))
        }
        var markReadResult: Result<Unit> = Result.success(Unit)
        var markAllResult: Result<Unit> = Result.success(Unit)
        val markedIds = mutableListOf<String>()
        var markAllCalled = false

        override suspend fun getNotifications(
            page: Int,
            size: Int,
            unreadOnly: Boolean
        ): Result<NotificationPage> {
            requestedPages += page to unreadOnly
            return responder(page, unreadOnly)
        }

        override suspend fun getUnreadCount(): Result<Int> = Result.success(0)

        override suspend fun markRead(id: String): Result<Unit> {
            markedIds += id
            return markReadResult
        }

        override suspend fun markAllRead(): Result<Unit> {
            markAllCalled = true
            return markAllResult
        }
    }
}
