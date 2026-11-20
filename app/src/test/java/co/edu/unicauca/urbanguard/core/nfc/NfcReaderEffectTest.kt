package co.edu.unicauca.urbanguard.core.nfc

import android.nfc.NfcAdapter
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNfcAdapter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NfcReaderEffectTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `reader remains active while NFC screen is visible and follows lifecycle`() {
        ShadowNfcAdapter.setNfcHardwareExists(true)
        Shadows.shadowOf(compose.activity.packageManager)
            .setSystemFeature(PackageManager.FEATURE_NFC, true)
        val adapter = NfcAdapter.getDefaultAdapter(compose.activity)
        val shadow = Shadows.shadowOf(adapter)
        shadow.setEnabled(true)
        val showScreen = mutableStateOf(true)
        val acceptReads = mutableStateOf(true)
        var acceptedReads = 0
        compose.setContent {
            if (showScreen.value) {
                NfcReaderEffect(enabled = true, onTagRead = {
                    if (acceptReads.value) acceptedReads++
                })
            }
        }

        compose.runOnIdle { assertTrue(shadow.isInReaderMode) }
        shadow.dispatchTagDiscovered(ShadowNfcAdapter.createMockTag())
        compose.runOnIdle { assertEquals(1, acceptedReads) }
        compose.runOnIdle { acceptReads.value = false }
        compose.runOnIdle { assertTrue(shadow.isInReaderMode) }
        shadow.dispatchTagDiscovered(ShadowNfcAdapter.createMockTag())
        compose.runOnIdle { assertEquals(1, acceptedReads) }

        compose.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        assertFalse(shadow.isInReaderMode)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.runOnIdle { assertTrue(shadow.isInReaderMode) }

        compose.runOnIdle { showScreen.value = false }
        compose.runOnIdle { assertFalse(shadow.isInReaderMode) }
    }
}
