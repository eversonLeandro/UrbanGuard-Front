package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.SavedStateHandle
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetOwnPqrsDetailUseCase
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetOwnPqrsUseCase
import co.edu.unicauca.urbanguard.ui.components.StatusTone
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
class MyPqrsViewModelsTest {
    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `historial muestra vacio general y permite refrescar`() = runTest {
        val repo = FakeResidentRepo()
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isGeneralEmpty)
        vm.onRefresh()
        advanceUntilIdle()
        assertEquals(2, repo.queries.size)
    }

    @Test fun `filtros usan query server y distinguien vacio filtrado`() = runTest {
        val repo = FakeResidentRepo().apply { mine = Result.success(listOf(summary())) }
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        advanceUntilIdle()
        repo.mine = Result.success(emptyList())
        repo.unfilteredMine = Result.success(listOf(summary()))
        vm.onStatusSelected(PqrsStatus.CLOSED)
        advanceUntilIdle()
        assertTrue(repo.queries.any { it.second == PqrsStatus.CLOSED })
        assertTrue(vm.uiState.value.isFilteredEmpty)
        vm.clearFilters()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hasFilters)
    }

    @Test fun `un error se presenta y puede reintentarse`() = runTest {
        val repo = FakeResidentRepo().apply {
            mine = Result.failure(AppException(AppError.Forbidden(AppError.Forbidden.MESSAGE)))
        }
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error is AppError.Forbidden)
        repo.mine = Result.success(listOf(summary()))
        vm.onRefresh()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.items.size)
    }

    @Test fun `sin conjunto activo historial evita consultar`() = runTest {
        val repo = FakeResidentRepo()
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession(""))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error is AppError.Validation)
        assertTrue(repo.queries.isEmpty())
    }

    @Test fun `detalle carga solo desde conjunto activo`() = runTest {
        val repo = FakeResidentRepo()
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")),
            GetOwnPqrsDetailUseCase(repo), activeSession()
        )
        advanceUntilIdle()
        assertEquals("pqrs-1", vm.uiState.value.detail?.id)
    }

    @Test fun `detalle 404 muestra mensaje propio`() = runTest {
        val repo = FakeResidentRepo().apply {
            ownDetail = Result.failure(AppException(AppError.NotFound(AppError.NotFound.MESSAGE)))
        }
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")),
            GetOwnPqrsDetailUseCase(repo), activeSession()
        )
        advanceUntilIdle()
        assertEquals("No encontramos esta PQRS.", vm.uiState.value.error?.message)
    }

    @Test fun `detalle sin conjunto activo evita consultar`() = runTest {
        val repo = FakeResidentRepo()
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")),
            GetOwnPqrsDetailUseCase(repo), activeSession("")
        )
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error is AppError.Validation)
    }

    @Test fun `historial limpia datos al cambiar usuario en el mismo conjunto`() = runTest {
        val repo = FakeResidentRepo().apply { mine = Result.success(listOf(summary())) }
        val session = activeSession()
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), session)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.items.size)
        repo.mine = Result.success(emptyList())
        session.saveSession(requireNotNull(session.session.value).copy(userId = "resident-2"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.items.isEmpty())
        assertEquals(2, repo.queries.size)
    }

    @Test fun `historial borra datos al perder rol residente`() = runTest {
        val repo = FakeResidentRepo().apply { mine = Result.success(listOf(summary())) }
        val session = activeSession()
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), session)
        advanceUntilIdle()
        session.saveSession(
            requireNotNull(session.session.value).copy(
                role = UserRole.ADMIN
            )
        )
        advanceUntilIdle()
        assertTrue(vm.uiState.value.items.isEmpty())
        assertTrue(vm.uiState.value.error is AppError.Forbidden)
        assertEquals(1, repo.queries.size)
    }

    @Test fun `detalle borra datos al cambiar usuario en mismo conjunto`() = runTest {
        val repo = FakeResidentRepo()
        val session = activeSession()
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")), GetOwnPqrsDetailUseCase(repo), session
        )
        advanceUntilIdle()
        assertEquals("pqrs-1", vm.uiState.value.detail?.id)
        repo.ownDetail = Result.failure(AppException(AppError.NotFound(AppError.NotFound.MESSAGE)))
        session.saveSession(requireNotNull(session.session.value).copy(userId = "resident-2"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.detail == null)
        assertTrue(vm.uiState.value.error is AppError.NotFound)
    }

    @Test fun `detalle niega rol administrador y id ausente`() = runTest {
        val repo = FakeResidentRepo()
        val session = activeSession()
        val vm = MyPqrsDetailViewModel(SavedStateHandle(), GetOwnPqrsDetailUseCase(repo), session)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error is AppError.NotFound)
        session.saveSession(requireNotNull(session.session.value).copy(role = UserRole.ADMIN))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error is AppError.Forbidden)
    }

    @Test fun `etiquetas y tonos incluyen rechazado y cerrado`() {
        assertEquals("Rechazada", PqrsStatus.REJECTED.spanishLabel)
        assertEquals("Cerrada", PqrsStatus.CLOSED.spanishLabel)
        assertEquals(StatusTone.ERROR, residentPqrsStatusTone(PqrsStatus.REJECTED))
        assertEquals(StatusTone.NEUTRAL, residentPqrsStatusTone(PqrsStatus.CLOSED))
        assertEquals(StatusTone.WARNING, residentPqrsStatusTone(PqrsStatus.SUBMITTED))
        assertEquals(StatusTone.WARNING, residentPqrsStatusTone(PqrsStatus.IN_REVIEW))
        assertEquals(StatusTone.INFO, residentPqrsStatusTone(PqrsStatus.IN_PROGRESS))
        assertEquals(StatusTone.SUCCESS, residentPqrsStatusTone(PqrsStatus.RESOLVED))
    }
}

private fun summary() = ResidentPqrsSummary(
    "pqrs-1", PqrsType.PETITION, "Asunto", PqrsStatus.SUBMITTED, "2026-10-30T12:00:00Z"
)
