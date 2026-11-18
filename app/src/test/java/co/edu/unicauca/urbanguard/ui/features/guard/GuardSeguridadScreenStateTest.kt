package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuardSeguridadScreenStateTest {
    @Test
    fun activeRoundKeepsContinueVisibleWhenTodayIsEmptyAndHistoryFails() {
        val active = PatrolRound(
            id = "active-yesterday",
            complexId = "complex-id",
            routeId = "route-id",
            routeName = "Ronda nocturna",
            startedAt = Instant.parse("2026-11-15T23:30:00Z")
        )

        assertFalse(
            GuardSeguridadUiState(activeRound = active, errorMessage = "No se pudo cargar Mis rondas")
                .hasBlockingError()
        )
        assertTrue(GuardSeguridadUiState(errorMessage = "No se pudo cargar").hasBlockingError())
        assertFalse(GuardSeguridadUiState().hasBlockingError())
    }
}
