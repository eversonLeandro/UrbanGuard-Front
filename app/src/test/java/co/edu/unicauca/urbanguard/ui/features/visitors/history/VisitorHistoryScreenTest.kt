package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w450dp-h900dp")
class VisitorHistoryScreenTest {
    @get:Rule val compose = createComposeRule()
    private val actions = mutableListOf<HistoryAction>()
    private val screenState = mutableStateOf(VisitorHistoryUiState())

    @Test
    fun `las tarjetas muestran autor cantidad localizada e ingreso real`() {
        val single = VisitorHistoryItem("1", "Juan Pérez", "Carlos Gómez", 1, ENTRY)
        val group = single.copy(
            invitationId = "2",
            creatorName = "Usuario",
            visitorName = "Andrea Ruiz",
            guestCount = 3,
            actualEntryAt = ENTRY.plusSeconds(60)
        )
        show(VisitorHistoryUiState(results = HistoryResults.Content(listOf(single, group), 1)))
        compose.onNodeWithText("Historial de visitantes").assertIsDisplayed()
        compose.onNodeWithText("Carlos Gómez").assertIsDisplayed()
        compose.onNodeWithText("Creada por Juan Pérez").assertIsDisplayed()
        compose.onNodeWithText("1 persona indicada en el pase").assertIsDisplayed()
        compose.onNodeWithText("Ingreso · 9 nov 2026, 2:35", substring = true).assertExists()
        scrollTo("3 personas indicadas en el pase")
        compose.onNodeWithText("Andrea Ruiz").assertIsDisplayed()
        compose.onNodeWithText("Creada por Usuario").assertIsDisplayed()
        compose.onNodeWithText("3 personas indicadas en el pase").assertIsDisplayed()
        compose.onNodeWithText("Ingreso · 9 nov 2026, 2:36", substring = true).assertExists()
        scrollTo("Cargar más")
        compose.onNodeWithText("Cargar más").performClick()
        scrollTo("Actualizar")
        compose.onNodeWithText("Actualizar").performClick()
        assertEquals(listOf(HistoryAction.LoadNext, HistoryAction.Refresh), actions)
    }

    @Test
    fun `filtros con fuente ampliada distinguen borrador resumen y borrar cada fecha`() {
        val draft = HistoryDates(DATE, DATE.plusDays(2))
        show(VisitorHistoryUiState(draft = draft, applied = HistoryDates(from = DATE.minusDays(1))), fontScale = 1.6f)
        compose.onNodeWithContentDescription("Desde").assertIsDisplayed()
        compose.onNodeWithContentDescription("Hasta").assertIsDisplayed()
        compose.onNodeWithContentDescription("Borrar fecha Desde").performClick()
        compose.onNodeWithContentDescription("Borrar fecha Hasta").performClick()
        compose.onNodeWithText("Desde el 8 nov 2026").assertIsDisplayed()
        compose.onNodeWithText("Aplicar").performClick()
        compose.onNodeWithText("Limpiar fechas").performClick()
        assertEquals(
            listOf(HistoryAction.EditFrom(null), HistoryAction.EditTo(null), HistoryAction.Apply, HistoryAction.Clear),
            actions
        )
        compose.onNodeWithContentDescription("Desde").performClick()
        compose.onNodeWithText("Aceptar").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
    }

    @Test
    fun `vacio filtrado ofrece limpiar y vacio general tiene mensaje propio`() {
        val dates = HistoryDates(to = DATE)
        val empty = HistoryResults.Content(emptyList(), null)
        show(VisitorHistoryUiState(draft = dates, applied = dates, results = empty))
        scrollTo("No se encontraron ingresos con las fechas aplicadas")
        compose.onNodeWithText("No se encontraron ingresos con las fechas aplicadas").assertIsDisplayed()
        scrollTo("Limpiar fechas")
        compose.onNodeWithText("Limpiar fechas").performClick()
        assertEquals(listOf(HistoryAction.Clear), actions)
        compose.runOnIdle { screenState.value = VisitorHistoryUiState(results = empty) }
        scrollTo("Todavía no hay ingresos de visitantes")
        compose.onNodeWithText("Todavía no hay ingresos de visitantes").assertIsDisplayed()
    }

    @Test
    fun `carga error inicial y error al pie conservan las acciones esperadas`() {
        val error = AppError.NoConnection(AppError.NoConnection.MESSAGE)
        show(VisitorHistoryUiState())
        compose.onNodeWithContentDescription("Cargando historial de visitantes").assertIsDisplayed()
        compose.runOnIdle { screenState.value = VisitorHistoryUiState(results = HistoryResults.Failed(error)) }
        scrollTo("Reintentar")
        compose.onNodeWithText("Reintentar").performClick()

        val entry = VisitorHistoryItem("1", "Juan", "Nombre largo del visitante para verificar lectura", 1, ENTRY)
        val page = HistoryResults.Content(listOf(entry), 1, HistoryOperation.LoadingNext)
        compose.runOnIdle { screenState.value = VisitorHistoryUiState(results = page) }
        compose.onNodeWithContentDescription("Cargando más ingresos").assertIsDisplayed()
        compose.runOnIdle {
            screenState.value = VisitorHistoryUiState(
                results = page.copy(operation = HistoryOperation.NextFailed(1, error))
            )
        }
        scrollTo("Reintentar")
        compose.onNodeWithText("Reintentar").performClick()
        scrollTo(entry.visitorName)
        compose.onNodeWithText(entry.visitorName).assertIsDisplayed()
        assertEquals(listOf(HistoryAction.Retry, HistoryAction.Retry), actions)
    }

    @Test
    fun `sin conjunto dirige al selector y sesion expirada usa la accion existente`() {
        var selectCalled = false
        var sessionCalled = false
        show(
            VisitorHistoryUiState(results = HistoryResults.NeedsComplex),
            select = { selectCalled = true },
            session = { sessionCalled = true }
        )
        compose.onNodeWithText("Selecciona un conjunto para ver el historial").assertIsDisplayed()
        compose.onNodeWithText("Seleccionar conjunto").performClick()
        assertTrue(selectCalled)
        compose.runOnIdle {
            screenState.value = VisitorHistoryUiState(
                results = HistoryResults.Unavailable(AppError.SessionExpired(AppError.SessionExpired.MESSAGE))
            )
        }
        compose.onNodeWithText("Iniciar sesión").performClick()
        assertTrue(sessionCalled)
    }

    private fun scrollTo(text: String) = compose.onNode(hasScrollAction()).performScrollToNode(hasText(text))

    private fun show(
        initial: VisitorHistoryUiState,
        fontScale: Float = 1f,
        select: () -> Unit = {},
        session: () -> Unit = {}
    ) {
        screenState.value = initial
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                VisitorHistoryContent(screenState.value, { actions += it }, {}, select, session)
            }
        }
    }

    private companion object {
        val ENTRY: Instant = Instant.parse("2026-11-09T19:35:00Z")
        val DATE: LocalDate = LocalDate.of(2026, 11, 9)
    }
}
