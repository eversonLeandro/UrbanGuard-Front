package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.IncidentSyncState
import co.edu.unicauca.urbanguard.domain.model.IncidentValidation
import co.edu.unicauca.urbanguard.domain.model.validateIncident
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests de novedades: validación del formulario y mapeo.
 */
class IncidentMapperTest {

    @Test
    fun validate_blankDescription_isRequired() {
        assertEquals(
            IncidentValidation.DESCRIPTION_REQUIRED,
            validateIncident(IncidentCategory.SECURITY, "  ")
        )
    }

    @Test
    fun validate_shortDescription_isTooShort() {
        assertEquals(
            IncidentValidation.DESCRIPTION_TOO_SHORT,
            validateIncident(IncidentCategory.SECURITY, "Roto")
        )
    }

    @Test
    fun validate_nullCategory_isRequired() {
        assertEquals(
            IncidentValidation.CATEGORY_REQUIRED,
            validateIncident(null, "Puerta del sótano abierta toda la noche")
        )
    }

    @Test
    fun validate_ok_isValid() {
        assertEquals(
            IncidentValidation.VALID,
            validateIncident(IncidentCategory.LIGHTING, "Luminaria fundida en torre 2")
        )
    }

    @Test
    fun incidentCategoryOf_unknown_defaultsToOther() {
        assertEquals(IncidentCategory.OTHER, incidentCategoryOf("RARO"))
        assertEquals(IncidentCategory.OTHER, incidentCategoryOf(null))
        assertEquals(IncidentCategory.SECURITY, incidentCategoryOf("security"))
    }

    @Test
    fun responseDto_roundTripKeepsPhotoAndState() {
        val dto = IncidentResponseDto(
            id = "n1",
            category = "SECURITY",
            description = "Puerta abierta",
            photoUrl = "https://x/foto.jpg",
            pointId = "p1",
            pointName = "Sótano",
            createdAt = "2026-02-10T22:05:00Z",
            synced = false
        )

        val domain = dto.toDomain("c1")

        assertEquals("n1", domain.id)
        assertEquals(IncidentCategory.SECURITY, domain.category)
        assertEquals("https://x/foto.jpg", domain.photoPath)
        assertEquals(IncidentSyncState.PENDING, domain.syncState)
        assertEquals(dto, domain.toResponseDto())
    }
}
