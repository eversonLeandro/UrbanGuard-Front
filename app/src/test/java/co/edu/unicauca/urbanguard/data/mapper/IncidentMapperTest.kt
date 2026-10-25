package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.IncidentResponseDto
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
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
            validateIncident(IncidentCategory.SECURITY, "  ", null)
        )
    }

    @Test
    fun validate_shortDescription_isTooShort() {
        assertEquals(
            IncidentValidation.DESCRIPTION_TOO_SHORT,
            validateIncident(IncidentCategory.SECURITY, "Roto", null)
        )
    }

    @Test
    fun validate_nullCategory_isRequired() {
        assertEquals(
            IncidentValidation.CATEGORY_REQUIRED,
            validateIncident(null, "Puerta del sótano abierta toda la noche", "photo.jpg")
        )
    }

    @Test
    fun validate_ok_isValid() {
        assertEquals(
            IncidentValidation.VALID,
            validateIncident(IncidentCategory.LIGHTING, "Luminaria fundida en torre 2", "photo.jpg")
        )
    }

    @Test
    fun validate_photoRequired() {
        assertEquals(
            IncidentValidation.PHOTO_REQUIRED,
            validateIncident(IncidentCategory.LIGHTING, "Luminaria fundida en torre 2", null)
        )
    }

    @Test
    fun incidentCategoryOf_unknown_defaultsToOther() {
        assertEquals(IncidentCategory.OTHER, incidentCategoryOf("RARO"))
        assertEquals(IncidentCategory.OTHER, incidentCategoryOf(null))
        assertEquals(IncidentCategory.SECURITY, incidentCategoryOf("security"))
    }

    @Test
    fun responseDto_mapsBackendReport() {
        val dto = IncidentResponseDto(
            reportId = "n1",
            complexId = "c1",
            guardId = "g1",
            category = "SECURITY",
            description = "Puerta abierta",
            reportedAt = "2026-02-10T22:05:00Z",
            checkpointId = "p1"
        )

        val domain = dto.toDomain("https://x/foto.jpg")

        assertEquals("n1", domain.id)
        assertEquals("c1", domain.complexId)
        assertEquals(IncidentCategory.SECURITY, domain.category)
        assertEquals("https://x/foto.jpg", domain.imageUrl)
        assertEquals("p1", domain.pointId)
        assertEquals("g1", domain.guardId)
    }
}
