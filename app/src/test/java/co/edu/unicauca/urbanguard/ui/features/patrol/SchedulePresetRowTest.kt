package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SchedulePresetRowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun presetsSelectTheirExpectedTimes() {
        val selected = mutableListOf<Pair<Int, Int>>()
        compose.setContent {
            SchedulePresetRow(enabled = true) { hour, minute ->
                selected += hour to minute
            }
        }

        compose.onNodeWithText("Mañana 08:00").performClick()
        compose.onNodeWithText("Noche 22:00").performClick()

        assertEquals(listOf(8 to 0, 22 to 0), selected)
    }

    @Test
    fun presetsAreDisabledWhenScheduleLimitIsReached() {
        val enabled = mutableStateOf(false)
        val selected = mutableListOf<Pair<Int, Int>>()
        compose.setContent {
            SchedulePresetRow(enabled = enabled.value) { hour, minute ->
                selected += hour to minute
            }
        }

        compose.onNodeWithText("Mañana 08:00").assertIsNotEnabled()
        compose.onNodeWithText("Noche 22:00").assertIsNotEnabled()
        compose.runOnIdle { enabled.value = true }
        compose.onNodeWithText("Mañana 08:00").assertIsEnabled().performClick()
        compose.runOnIdle { enabled.value = false }
        compose.onNodeWithText("Mañana 08:00").assertIsNotEnabled()
        compose.onNodeWithText("Noche 22:00").assertIsNotEnabled()
        assertEquals(listOf(8 to 0), selected)
    }

    @Test
    fun presetsRemainUsableWhenSurroundingContentChanges() {
        val counter = mutableStateOf(0)
        val selected = mutableListOf<Pair<Int, Int>>()
        val onPreset: (Int, Int) -> Unit = { hour, minute -> selected += hour to minute }
        compose.setContent {
            Text("Cambio ${counter.value}")
            SchedulePresetRow(enabled = true, onPreset = onPreset)
        }

        compose.runOnIdle { counter.value++ }
        compose.onNodeWithText("Cambio 1").assertExists()
        compose.onNodeWithText("Noche 22:00").performClick()
        assertEquals(listOf(22 to 0), selected)
    }
}
