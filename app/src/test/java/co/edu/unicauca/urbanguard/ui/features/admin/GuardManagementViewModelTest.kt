package co.edu.unicauca.urbanguard.ui.features.admin

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.domain.model.MembershipRole
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import co.edu.unicauca.urbanguard.domain.usecase.guards.GetGuardMembershipsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.guards.SetGuardMembershipActiveUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuardManagementViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carga estado por defecto y recarga al cambiar filtro`() = runTest {
        val repository = FakeGuardRepository()
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        assertEquals(listOf("complex-1" to "activos"), repository.listCalls)

        viewModel.onStatusFilterChange(GuardStatusFilter.INACTIVE)
        advanceUntilIdle()

        assertEquals("inactivos", repository.listCalls.last().second)
        assertEquals(GuardStatusFilter.INACTIVE, viewModel.uiState.value.statusFilter)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `inactiva en el conjunto activo y actualiza la lista`() = runTest {
        val repository = FakeGuardRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onChangeStatus(GUARD)
        advanceUntilIdle()

        assertEquals("complex-1", repository.lastChangedComplexId)
        assertEquals(GUARD.membershipId, repository.lastChangedMembershipId)
        assertEquals(false, repository.lastActive)
        assertEquals("Vigilante inactivado en este conjunto.", viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.changingMembershipId)
        assertEquals(2, repository.listCalls.size)
    }

    @Test
    fun `rechaza la gestion desde una sesion no administradora`() = runTest {
        val repository = FakeGuardRepository()
        val viewModel = createViewModel(repository, UserRole.GUARD)
        advanceUntilIdle()

        viewModel.onChangeStatus(GUARD)
        advanceUntilIdle()

        assertTrue(repository.listCalls.isEmpty())
        assertEquals(0, repository.changeCalls)
        assertEquals("Solo un administrador puede gestionar los vigilantes.",
            viewModel.uiState.value.errorMessage)
    }

    private fun createViewModel(
        repository: FakeGuardRepository,
        role: UserRole = UserRole.ADMIN
    ): GuardManagementViewModel {
        val sessionManager = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("token", "admin-1", "Administrador", role))
            setAssociations(listOf(ConjuntoUserAssociation("complex-1", "Conjunto", role)))
            setActiveConjunto("complex-1")
        }
        return GuardManagementViewModel(
            GetGuardMembershipsUseCase(repository),
            SetGuardMembershipActiveUseCase(repository),
            sessionManager
        )
    }

    private class FakeGuardRepository : GuardRepository {
        val listCalls = mutableListOf<Pair<String, String>>()
        var changeCalls = 0
        var lastChangedComplexId: String? = null
        var lastChangedMembershipId: String? = null
        var lastActive: Boolean? = null

        override suspend fun registerGuard(
            complexId: String,
            email: String,
            nombres: String,
            apellidos: String,
            telefono: String
        ): Result<CreatedGuard> = Result.failure(UnsupportedOperationException())

        override suspend fun getGuards(
            complexId: String,
            status: String
        ): Result<List<GuardMembership>> {
            listCalls += complexId to status
            return Result.success(listOf(GUARD))
        }

        override suspend fun setGuardActive(
            complexId: String,
            membershipId: String,
            active: Boolean
        ): Result<Unit> {
            changeCalls++
            lastChangedComplexId = complexId
            lastChangedMembershipId = membershipId
            lastActive = active
            return Result.success(Unit)
        }
    }

    private companion object {
        val GUARD = GuardMembership("membership-1", "user-1", "guard@example.com", "Carlos Pérez", true)
    }
}
