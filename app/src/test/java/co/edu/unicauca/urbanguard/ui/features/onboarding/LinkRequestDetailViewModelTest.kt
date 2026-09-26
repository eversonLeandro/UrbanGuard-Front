package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.SavedStateHandle
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.data.remote.api.dto.ComplexSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.service.CommunityApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoAssociationRepository
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetLinkRequestDetailUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.ReviewLinkRequestUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LinkRequestDetailViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `aprobar solicitud completamente sincronizada muestra mensaje de exito total`() = runTest {
        val repo = FakeLinkRequestRepository(
            detailRequest = LinkRequest(id = "req1", nombreSolicitante = "Juan"),
            approvedResult = LinkRequest(id = "req1", roleSyncPending = false, organizationSyncPending = false)
        )
        val viewModel = createViewModel(repo)

        viewModel.onApproveConfirmed()
        advanceUntilIdle()

        assertEquals(
            "Solicitud aceptada correctamente. El usuario fue agregado como residente.",
            viewModel.uiState.value.successMessage
        )
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `aprobar solicitud con sincronizacion pendiente en Auth0 muestra mensaje de advertencia`() = runTest {
        val repo = FakeLinkRequestRepository(
            detailRequest = LinkRequest(id = "req1", nombreSolicitante = "Juan"),
            approvedResult = LinkRequest(id = "req1", roleSyncPending = true, organizationSyncPending = true)
        )
        val viewModel = createViewModel(repo)

        viewModel.onApproveConfirmed()
        advanceUntilIdle()

        assertEquals(
            "Solicitud aprobada localmente, pero la sincronización con Auth0 quedó pendiente.",
            viewModel.uiState.value.successMessage
        )
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `error al aprobar muestra mensaje de error en uiState`() = runTest {
        // El repositorio real falla con un AppException ya traducido, no con una
        // Exception suelta: ese es el contrato que consume la UI.
        val repo = FakeLinkRequestRepository(
            detailRequest = LinkRequest(id = "req1", nombreSolicitante = "Juan"),
            approveError = AppException(AppError.NoConnection(AppError.NoConnection.MESSAGE))
        )
        val viewModel = createViewModel(repo)

        viewModel.onApproveConfirmed()
        advanceUntilIdle()

        assertEquals(AppError.NoConnection.MESSAGE, viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `un error que no viene de la capa de datos no filtra texto tecnico al usuario`() = runTest {
        val repo = FakeLinkRequestRepository(
            detailRequest = LinkRequest(id = "req1", nombreSolicitante = "Juan"),
            approveError = IllegalStateException("java.lang.NullPointerException at Foo.kt:42")
        )
        val viewModel = createViewModel(repo)

        viewModel.onApproveConfirmed()
        advanceUntilIdle()

        assertEquals(AppError.Unexpected.MESSAGE, viewModel.uiState.value.errorMessage)
    }

    private fun createViewModel(linkRepo: LinkRequestRepository): LinkRequestDetailViewModel {
        return LinkRequestDetailViewModel(
            getLinkRequestDetailUseCase = GetLinkRequestDetailUseCase(linkRepo),
            reviewLinkRequestUseCase = ReviewLinkRequestUseCase(linkRepo),
            conjuntoAssociationRepository = FakeConjuntoAssociationRepository(),
            sessionManager = SessionManager(NoOpRolePersistence),
            communityApi = FakeCommunityApi(),
            savedStateHandle = SavedStateHandle(mapOf("requestId" to "req1"))
        )
    }

    private class FakeLinkRequestRepository(
        private val detailRequest: LinkRequest,
        private val approvedResult: LinkRequest? = null,
        private val approveError: Exception? = null
    ) : LinkRequestRepository {

        override suspend fun getRequests(): Result<List<LinkRequest>> {
            return Result.success(listOf(detailRequest))
        }

        override suspend fun getRequestById(id: String): Result<LinkRequest> {
            return Result.success(detailRequest)
        }

        override suspend fun reviewRequest(id: String, approve: Boolean): Result<LinkRequest> {
            if (approveError != null) return Result.failure(approveError)
            return Result.success(approvedResult ?: detailRequest)
        }
    }

    private class FakeConjuntoAssociationRepository : ConjuntoAssociationRepository {
        override suspend fun getMyConjuntos(): Result<List<ConjuntoUserAssociation>> {
            return Result.success(emptyList())
        }
    }

    private class FakeCommunityApi : CommunityApi {
        override suspend fun getMyComplexes(): List<ComplexSummaryDto> = emptyList()
        override suspend fun refreshCommunityContext(): Response<Unit> = Response.success(Unit)
    }
}
