package co.edu.unicauca.urbanguard.ui.features.patrol

import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.SavePatrolPointUseCase
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
class PatrolPointFormNfcTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `tag is accepted only while waiting but reader remains active afterwards`() {
        ShadowNfcAdapter.setNfcHardwareExists(true)
        Shadows.shadowOf(compose.activity.packageManager)
            .setSystemFeature(PackageManager.FEATURE_NFC, true)
        val shadow = Shadows.shadowOf(NfcAdapter.getDefaultAdapter(compose.activity))
        shadow.setEnabled(true)
        val repository = FakeRepository()
        val viewModel = PatrolPointFormViewModel(
            GetPatrolPointsUseCase(repository),
            SavePatrolPointUseCase(repository),
            SessionManager(NoOpRolePersistence)
        )
        compose.setContent {
            PatrolPointFormScreen(
                pointId = null,
                onSaved = {},
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        compose.runOnIdle { assertTrue(shadow.isInReaderMode) }

        compose.runOnIdle {
            viewModel.onFieldChange(name = "Entrada")
            viewModel.onNext()
            viewModel.onStartNfcRead(true)
        }
        compose.waitUntil(10_000) { viewModel.uiState.value.nfcState == NfcReadState.WAITING }
        shadow.dispatchTagDiscovered(ShadowNfcAdapter.createMockTag())
        compose.waitUntil(10_000) { viewModel.uiState.value.nfcState == NfcReadState.READ }
        compose.runOnIdle { assertTrue(shadow.isInReaderMode) }

        compose.runOnIdle { viewModel.onTagIdChange("AABBCCDD") }
        shadow.dispatchTagDiscovered(ShadowNfcAdapter.createMockTag())
        compose.runOnIdle { assertEquals("AABBCCDD", viewModel.uiState.value.nfcTagId) }
    }

    @Test
    fun `two immediate NFC callbacks keep the first tag`() {
        val repository = FakeRepository()
        val viewModel = PatrolPointFormViewModel(
            GetPatrolPointsUseCase(repository),
            SavePatrolPointUseCase(repository),
            SessionManager(NoOpRolePersistence)
        )
        viewModel.onNfcTagRead("00112233")
        assertEquals("", viewModel.uiState.value.nfcTagId)

        viewModel.onFieldChange(name = "Entrada")
        viewModel.onNext()
        viewModel.onStartNfcRead(true)
        viewModel.onNfcTagRead("AABBCCDD")
        viewModel.onNfcTagRead("11223344")

        assertEquals("AABBCCDD", viewModel.uiState.value.nfcTagId)
        assertEquals(NfcReadState.READ, viewModel.uiState.value.nfcState)
    }

    private class FakeRepository : PatrolRepository {
        override suspend fun getPoints(complexId: String): Result<List<PatrolControlPoint>> =
            Result.success(emptyList())

        override suspend fun savePoint(point: PatrolControlPoint): Result<PatrolControlPoint> =
            Result.success(point)

        override suspend fun setPointActive(
            complexId: String,
            pointId: String,
            isActive: Boolean
        ): Result<Unit> = Result.success(Unit)
    }
}
