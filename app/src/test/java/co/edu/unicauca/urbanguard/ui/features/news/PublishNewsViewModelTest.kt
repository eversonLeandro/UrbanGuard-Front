package co.edu.unicauca.urbanguard.ui.features.news

import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import co.edu.unicauca.urbanguard.domain.usecase.news.PublishNewsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.RequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.OffsetDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class PublishNewsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `publish sets error when invalid`() = runTest {
        val sessionManager = sessionWithComplex()
        val viewModel = PublishNewsViewModel(
            PublishNewsUseCase(FakeNewsRepository()),
            sessionManager,
            android.app.Application()
        )
        viewModel.onTitleChange("")
        viewModel.onBodyChange("")
        viewModel.publish()
        assertTrue(viewModel.uiState.value.errorMessage != null)
        assertFalse(viewModel.uiState.value.isPublished)
    }

    @Test
    fun `publish success sets isPublished`() = runTest {
        val sessionManager = sessionWithComplex()
        val viewModel = PublishNewsViewModel(
            PublishNewsUseCase(FakeNewsRepository()),
            sessionManager,
            android.app.Application()
        )
        viewModel.onTitleChange("titulo")
        viewModel.onBodyChange("cuerpo")
        viewModel.publish()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPublished)
    }

    @Test
    fun `publish error sets errorMessage`() = runTest {
        val sessionManager = sessionWithComplex()
        val viewModel = PublishNewsViewModel(
            PublishNewsUseCase(FakeNewsRepository(error = RuntimeException("error backend"))),
            sessionManager,
            android.app.Application()
        )
        viewModel.onTitleChange("titulo")
        viewModel.onBodyChange("cuerpo")
        viewModel.publish()
        advanceUntilIdle()
        assertEquals("error backend", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isPublished)
    }

    @Test
    fun `publish with attachment larger than max sets error and does not publish`() = runTest {
        val sessionManager = sessionWithComplex()
        val repo = FakeNewsRepository()
        val viewModel = PublishNewsViewModel(
            PublishNewsUseCase(repo),
            sessionManager,
            android.app.Application()
        )
        viewModel.onTitleChange("titulo")
        viewModel.onBodyChange("cuerpo")
        // Simulate attachment metadata indicating oversized file
        val stateField = viewModel::class.java.getDeclaredField("_uiState")
        stateField.isAccessible = true
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<PublishNewsUiState>
        stateFlow.value = stateFlow.value.copy(
            attachmentUri = android.net.Uri.parse("content://test/grande.pdf"),
            attachmentSize = co.edu.unicauca.urbanguard.core.common.NewsConstants.ATTACHMENT_MAX_BYTES + 1,
            attachmentMime = "application/pdf",
            attachmentName = "grande.pdf"
        )
        viewModel.publish()
        advanceUntilIdle()
        assertEquals(
            co.edu.unicauca.urbanguard.core.common.NewsConstants.ATTACH_TOO_LARGE,
            viewModel.uiState.value.errorMessage
        )
        assertFalse(viewModel.uiState.value.isPublished)
    }

    private fun sessionWithComplex(): SessionManager {
        return SessionManager(FakeRolePersistence()).apply {
            saveSession(
                UserSession(
                    token = "t",
                    userId = "u1",
                    name = "User",
                    role = UserRole.ADMIN,
                    conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "Conjunto", UserRole.ADMIN)),
                    conjuntoActivoId = "c1"
                )
            )
        }
    }

    private class FakeNewsRepository(
        private val error: Throwable? = null
    ) : NewsRepository {
        override suspend fun getNews(complexId: String): Result<List<News>> = Result.success(emptyList())
        override suspend fun publishNews(
            complexId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?
        ): Result<News> {
            error?.let { return Result.failure(it) }
            return Result.success(
                News(
                    announcementId = "a1",
                    title = title,
                    body = body,
                    publishedAt = OffsetDateTime.now(),
                    attachment = null
                )
            )
        }
        override suspend fun updateNews(
            complexId: String,
            announcementId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?,
            removeAttachment: Boolean
        ): Result<News> {
            return Result.success(
                News(
                    announcementId = announcementId,
                    title = title,
                    body = body,
                    publishedAt = OffsetDateTime.now(),
                    attachment = null
                )
            )
        }
        override suspend fun deleteNews(complexId: String, announcementId: String): Result<Unit> {
            return Result.success(Unit)
        }
        override suspend fun downloadAttachment(complexId: String, announcementId: String): Result<File> {
            return Result.failure(RuntimeException("no"))
        }
    }

    private class FakeRolePersistence : co.edu.unicauca.urbanguard.core.session.RolePersistence {
        private var stored: co.edu.unicauca.urbanguard.core.session.PersistedRole? = null
        override fun save(role: co.edu.unicauca.urbanguard.core.session.PersistedRole) { stored = role }
        override fun read(): co.edu.unicauca.urbanguard.core.session.PersistedRole? = stored
        override fun clear() { stored = null }
    }
}
