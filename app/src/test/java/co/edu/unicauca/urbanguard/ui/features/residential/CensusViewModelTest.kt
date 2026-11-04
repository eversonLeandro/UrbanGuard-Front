package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import co.edu.unicauca.urbanguard.domain.usecase.residential.GetCensusUseCase
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
            ResidentialUnit("unit-1", "101", listOf(ResidentialResident("user-1", "Ana Pérez"))),
            ResidentialUnit("unit-2", "102", emptyList())
        )
        val repository = FakeResidentialRepository(Result.success(expected))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals("complex-1", repository.lastComplexId)
        assertEquals(expected, viewModel.uiState.value.units)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.error == null)
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
                        listOf(ResidentialResident("user-1", "Ana Pérez"))
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
            listOf(ResidentialResident("user-1", "Ana Pérez"))
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

    private fun createViewModel(repository: FakeResidentialRepository): CensusViewModel {
        val sessionManager = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("token", "user-1", "Admin", UserRole.ADMIN))
            setAssociations(
                listOf(ConjuntoUserAssociation("complex-1", "Conjunto", UserRole.ADMIN))
            )
            setActiveConjunto("complex-1")
        }
        return CensusViewModel(GetCensusUseCase(repository), sessionManager)
    }
}

private class FakeResidentialRepository(
    var result: Result<List<ResidentialUnit>>
) : ResidentialRepository {
    var calls = 0
    var lastComplexId: String? = null

    override suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>> {
        calls++
        lastComplexId = complexId
        return result
    }
}
