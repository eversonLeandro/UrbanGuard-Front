package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CurrentTagDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolPointValidation
import co.edu.unicauca.urbanguard.domain.model.isValidNfcUid
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import co.edu.unicauca.urbanguard.domain.model.sortedForRoute
import co.edu.unicauca.urbanguard.domain.model.validatePatrolPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests de validación, orden local y mapeo de checkpoints. */
class PatrolPointMapperTest {

    private fun makePoint(
        id: String = "p1",
        name: String = "Portería norte",
        tag: String = "04A3F2B1",
        isActive: Boolean = true
    ) = PatrolControlPoint(
        id = id,
        complexId = "c1",
        name = name,
        location = "Torre 1",
        description = "",
        nfcTagId = tag,
        isActive = isActive
    )

    @Test
    fun validate_blankName_isRequired() {
        assertEquals(
            PatrolPointValidation.NAME_REQUIRED,
            validatePatrolPoint("  ", "04A3F2B1")
        )
    }

    @Test
    fun validate_longName_isTooLong() {
        val name = "a".repeat(101)
        assertEquals(
            PatrolPointValidation.NAME_TOO_LONG,
            validatePatrolPoint(name, "04A3F2B1")
        )
    }

    @Test
    fun validate_blankTag_isRequired() {
        assertEquals(
            PatrolPointValidation.TAG_REQUIRED,
            validatePatrolPoint("Punto", "  ")
        )
    }

    @Test
    fun validate_invalidTag_isInvalid() {
        assertEquals(
            PatrolPointValidation.TAG_INVALID,
            validatePatrolPoint("Punto", "XYZ")
        )
    }

    @Test
    fun validate_ok_isValid() {
        assertEquals(
            PatrolPointValidation.VALID,
            validatePatrolPoint("Punto", "04:A3:F2:B1")
        )
    }

    @Test
    fun isValidNfcUid_acceptsKnownLengthsOnly() {
        assertTrue(isValidNfcUid("04A3F2B1"))
        assertTrue(isValidNfcUid("04:A3:F2:B1:8C:50:12"))
        assertFalse(isValidNfcUid("04A3"))
    }

    @Test
    fun normalizeNfcUid_removesSeparators() {
        assertEquals("04A3F2B1", normalizeNfcUid(" 04:a3-f2 b1 "))
    }

    @Test
    fun sortedForRoute_activeFirstThenByName() {
        val points = listOf(
            makePoint("inactive", name = "A", isActive = false),
            makePoint("second", name = "B"),
            makePoint("first", name = "A")
        )

        val sorted = points.sortedForRoute().map { it.id }

        assertEquals(listOf("first", "second", "inactive"), sorted)
    }

    @Test
    fun checkpointDto_mapsCurrentTagAndFields() {
        val dto = CheckpointResponseDto(
            checkpointId = "p1",
            name = "Portería norte",
            description = "Entrada",
            location = "Torre 1",
            active = false,
            currentTag = CurrentTagDto(
                assignmentId = "a1",
                uid = "04:a3:f2:b1"
            )
        )

        val domain = dto.toDomain("c1")

        assertEquals("p1", domain.id)
        assertEquals("c1", domain.complexId)
        assertEquals("Entrada", domain.description)
        assertEquals("04A3F2B1", domain.nfcTagId)
        assertFalse(domain.isActive)
    }

    @Test
    fun patchBody_onlyIncludesChangedFields() {
        val current = makePoint()
        val edited = current.copy(description = "", location = "Torre 2")

        val patch = edited.toPatchBody(current)

        assertFalse(patch.has("name"))
        assertFalse(patch.has("active"))
        assertFalse(patch.has("description"))
        assertEquals("Torre 2", patch.get("location").asString)
    }
}
