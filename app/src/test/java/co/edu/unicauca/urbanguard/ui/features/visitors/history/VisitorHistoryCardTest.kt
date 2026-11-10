package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryItem
import co.edu.unicauca.urbanguard.ui.theme.UrbanGuardFrontendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w320dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VisitorHistoryCardTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `tarjeta informativa conserva datos largos y encabezado con fuente ampliada en ambos temas`() {
        val item = VisitorHistoryItem(
            invitationId = "visitor-1",
            creatorName = "María Fernanda de los Ángeles Rodríguez de la Cruz",
            visitorName = "Carlos Andrés de los Santos Gómez del Valle",
            guestCount = 4,
            actualEntryAt = Instant.parse("2026-11-09T19:35:00Z")
        )
        val dark = mutableStateOf(false)
        compose.setContent {
            UrbanGuardFrontendTheme(darkTheme = dark.value, dynamicColor = false) {
                val density = LocalDensity.current.density
                CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 1.6f)) {
                    Box(Modifier.width(300.dp)) { VisitorHistoryCard(item) }
                }
            }
        }

        assertReadableCard(item)
        compose.runOnIdle { dark.value = true }
        assertReadableCard(item)
    }

    private fun assertReadableCard(item: VisitorHistoryItem) {
        val title = compose.onNode(
            hasText(item.visitorName) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading) and
                !hasClickAction() and !hasAnyAncestor(hasClickAction())
        )
        title.assertTextCanWrap()
        for (text in listOf(
            "Ingreso · ${VisitorHistoryDates.entry(item.actualEntryAt)}",
            "Creada por ${item.creatorName}",
            "4 personas indicadas en el pase"
        )) {
            compose.onNodeWithText(text).assertTextCanWrap()
        }
    }

    private fun SemanticsNodeInteraction.assertTextCanWrap() {
        val layouts = mutableListOf<TextLayoutResult>()
        assertIsDisplayed().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        assertTrue(layout.layoutInput.softWrap)
        assertEquals(Int.MAX_VALUE, layout.layoutInput.maxLines)
        assertFalse(layout.hasVisualOverflow)
    }
}
