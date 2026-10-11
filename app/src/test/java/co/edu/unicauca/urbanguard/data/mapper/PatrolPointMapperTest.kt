package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointResponseDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolPointValidation
import co.edu.unicauca.urbanguard.domain.model.sortedForRoute
import co.edu.unicauca.urbanguard.domain.model.validatePatrolPoint
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests de puntos de control: validación del formulario, orden de la ruta
 * y mapeo con los DTOs (el mismo que usará el backend).
 */
class PatrolPointMapperTest {

    private fun makePoint(
        id: String = "p1",
        order: Int = 0,
        isActive: Boolean = true
    ) = PatrolControlPoint(
        id = id,
        complexId = "c1",
        name = "Portería norte",
        location = "Torre 1",
        nfcTagId = "04A3F2",
        order = order,
        isActive = isActive
    )

    @Test
    fun validate_blankName_isRequired() {
        assertEquals(PatrolPointValidation.NAME_REQUIRED, validatePatrolPoint("  ", "04A3"))
    }

    @Test
    fun validate_longName_isTooLong() {
        val name = "a".repeat(101)
        assertEquals(PatrolPointValidation.NAME_TOO_LONG, validatePatrolPoint(name, "04A3"))
    }

    @Test
    fun validate_blankTag_isRequired() {
        assertEquals(PatrolPointValidation.TAG_REQUIRED, validatePatrolPoint("Punto", "  "))
    }

    @Test
    fun validate_ok_isValid() {
        assertEquals(PatrolPointValidation.VALID, validatePatrolPoint("Punto", "04A3"))
    }

    @Test
    fun sortedForRoute_activeFirstThenByOrder() {
        val points = listOf(
            makePoint("inactive-first", order = 0, isActive = false),
            makePoint("second", order = 1),
            makePoint("first", order = 0)
        )

        val sorted = points.sortedForRoute().map { it.id }

        assertEquals(listOf("first", "second", "inactive-first"), sorted)
    }

    @Test
    fun responseDto_roundTripKeepsFields() {
        val dto = PatrolPointResponseDto(
            id = "p1",
            name = "Portería norte",
            location = "Torre 1",
            nfcTagId = "04A3F2",
            order = 2,
            isActive = false
        )

        val domain = dto.toDomain("c1")

        assertEquals("p1", domain.id)
        assertEquals("c1", domain.complexId)
        assertEquals(2, domain.order)
        assertEquals(false, domain.isActive)
        assertEquals(dto, domain.toResponseDto())
    }
}
