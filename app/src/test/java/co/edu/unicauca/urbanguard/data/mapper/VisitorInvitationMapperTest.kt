package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorInvitationListItemDto
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitationStatus
import co.edu.unicauca.urbanguard.domain.model.sortedForResident
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests del mapeo del listado de invitaciones y de su orden en pantalla.
 */
class VisitorInvitationMapperTest {

    private fun makeDto(
        id: String = "inv-1",
        status: String? = "ACTIVE",
        visitDate: String = "2026-02-10"
    ) = VisitorInvitationListItemDto(
        invitationId = id,
        visitorName = "Ana Ruiz",
        guestCount = 2,
        visitDate = visitDate,
        status = status
    )

    private fun makeInvitation(
        id: String,
        status: VisitorInvitationStatus,
        visitDate: String
    ) = VisitorInvitation(
        invitationId = id,
        visitorName = "Test",
        guestCount = 1,
        visitDate = LocalDate.parse(visitDate),
        status = status
    )

    @Test
    fun toVisitorInvitation_mapsAllFields() {
        val domain = makeDto().toVisitorInvitation()

        assertEquals("inv-1", domain.invitationId)
        assertEquals("Ana Ruiz", domain.visitorName)
        assertEquals(2, domain.guestCount)
        assertEquals(LocalDate.parse("2026-02-10"), domain.visitDate)
        assertEquals(VisitorInvitationStatus.ACTIVE, domain.status)
    }

    @Test
    fun fromBackend_mapsKnownStatuses() {
        assertEquals(VisitorInvitationStatus.ACTIVE, VisitorInvitationStatus.fromBackend("ACTIVE"))
        assertEquals(
            VisitorInvitationStatus.PROGRAMMED,
            VisitorInvitationStatus.fromBackend("PROGRAMMED")
        )
        assertEquals(VisitorInvitationStatus.EXPIRED, VisitorInvitationStatus.fromBackend("EXPIRED"))
        assertEquals(VisitorInvitationStatus.USED, VisitorInvitationStatus.fromBackend("USED"))
    }

    @Test
    fun fromBackend_unknownOrNull_defaultsToUsed() {
        assertEquals(VisitorInvitationStatus.USED, VisitorInvitationStatus.fromBackend("OTRO"))
        assertEquals(VisitorInvitationStatus.USED, VisitorInvitationStatus.fromBackend(null))
    }

    @Test
    fun isShareable_onlyActiveAndProgrammed() {
        assertTrue(VisitorInvitationStatus.ACTIVE.isShareable)
        assertTrue(VisitorInvitationStatus.PROGRAMMED.isShareable)
        assertFalse(VisitorInvitationStatus.USED.isShareable)
        assertFalse(VisitorInvitationStatus.EXPIRED.isShareable)
    }

    @Test
    fun sortedForResident_activeFirstThenProgrammed() {
        val invitations = listOf(
            makeInvitation("used", VisitorInvitationStatus.USED, "2026-02-09"),
            makeInvitation("expired", VisitorInvitationStatus.EXPIRED, "2026-02-08"),
            makeInvitation("programmed", VisitorInvitationStatus.PROGRAMMED, "2026-02-12"),
            makeInvitation("active", VisitorInvitationStatus.ACTIVE, "2026-02-10")
        )

        val sorted = invitations.sortedForResident().map { it.invitationId }

        assertEquals(listOf("active", "programmed", "used", "expired"), sorted)
    }
}
