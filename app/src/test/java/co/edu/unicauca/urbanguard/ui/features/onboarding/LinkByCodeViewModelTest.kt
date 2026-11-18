package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.core.push.PushRegistrar
import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.model.UnitOption
import co.edu.unicauca.urbanguard.domain.repository.LinkByCodeRepository
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.RequestLinkUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.SearchConjuntoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LinkByCodeViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `envia el codigo digitado al solicitar la vinculacion`() = runTest {
        val repository = FakeLinkByCodeRepository()
        val viewModel = createViewModel(repository)

        viewModel.onCodeChange(" k7m3px ")
        viewModel.onSearchClick()
        advanceUntilIdle()
        viewModel.onApartamentoChange("101")
        viewModel.onRequestLinkClick()
        advanceUntilIdle()

        assertEquals("k7m3px", repository.lastCodigo)
        assertEquals("complex-1", repository.lastConjuntoId)
        assertEquals("unit-1", repository.lastUnitId)
        assertTrue(viewModel.uiState.value.requestSuccess)
    }

    @Test
    fun `no solicita vinculacion si el codigo quedo vacio`() = runTest {
        val repository = FakeLinkByCodeRepository()
        val viewModel = createViewModel(repository)

        viewModel.onCodeChange("k7m3px")
        viewModel.onSearchClick()
        advanceUntilIdle()
        viewModel.onApartamentoChange("101")
        viewModel.onCodeChange("   ")
        viewModel.onRequestLinkClick()
        advanceUntilIdle()

        assertEquals(0, repository.requestCalls)
        assertEquals("Ingresa el código del conjunto", viewModel.uiState.value.codeError)
    }

    private fun createViewModel(repository: FakeLinkByCodeRepository): LinkByCodeViewModel =
        LinkByCodeViewModel(
            searchConjuntoUseCase = SearchConjuntoUseCase(repository),
            requestLinkUseCase = RequestLinkUseCase(repository),
            pushRegistrar = NoOpPushRegistrar
        )

    private class FakeLinkByCodeRepository : LinkByCodeRepository {
        var lastConjuntoId: String? = null
        var lastUnitId: String? = null
        var lastCodigo: String? = null
        var requestCalls = 0

        override suspend fun searchConjunto(code: String): Result<ConjuntoSearch> =
            Result.success(
                ConjuntoSearch(
                    id = "complex-1",
                    nombre = "Conjunto Test",
                    direccion = "Calle 1",
                    ciudad = "Popayán",
                    totalMiembros = 10,
                    unidades = listOf(UnitOption("unit-1", "101"))
                )
            )

        override suspend fun requestLink(
            conjuntoId: String,
            unitId: String,
            codigo: String
        ): Result<LinkRequest> {
            requestCalls++
            lastConjuntoId = conjuntoId
            lastUnitId = unitId
            lastCodigo = codigo
            return Result.success(LinkRequest(id = "request-1"))
        }
    }

    private object NoOpPushRegistrar : PushRegistrar {
        override fun registerActiveConjunto() = Unit
        override fun registerForConjunto(conjuntoId: String) = Unit
        override fun onTokenRotated(token: String) = Unit
        override fun unregisterCurrent(onComplete: () -> Unit) = onComplete()
    }
}
