package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialResidentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialUnitDto
import co.edu.unicauca.urbanguard.data.remote.api.service.ResidentialApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResidentialRepositoryImplTest {

    @Test
    fun `getCensus envia el conjunto y conserva unidades vacias`() = runTest {
        val api = FakeResidentialApi(
            units = listOf(
                ResidentialUnitDto(
                    id = "unit-1",
                    identifier = "101",
                    residents = listOf(ResidentialResidentDto("user-1", "Ana Pérez"))
                ),
                ResidentialUnitDto(id = "unit-2", identifier = "102", residents = emptyList())
            )
        )

        val result = ResidentialRepositoryImpl(api).getCensus("complex-1")

        assertTrue(result.isSuccess)
        assertEquals("complex-1", api.lastComplexId)
        assertEquals("Ana Pérez", result.getOrThrow().first().residents.single().fullName)
        assertTrue(result.getOrThrow().last().residents.isEmpty())
    }

    @Test
    fun `getCensus sin conjunto retorna validacion sin llamar la API`() = runTest {
        val api = FakeResidentialApi()

        val result = ResidentialRepositoryImpl(api).getCensus("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is co.edu.unicauca.urbanguard.domain.model.AppException)
        assertEquals(0, api.calls)
    }

    @Test
    fun `getCensus rechaza respuestas incompletas`() = runTest {
        val api = FakeResidentialApi(
            units = listOf(ResidentialUnitDto(id = "unit-1", identifier = null, residents = emptyList()))
        )

        val result = ResidentialRepositoryImpl(api).getCensus("complex-1")

        assertTrue(result.isFailure)
        assertTrue((result.exceptionOrNull() as co.edu.unicauca.urbanguard.domain.model.AppException).error is AppError.Unexpected)
    }
}

private class FakeResidentialApi(
    private val units: List<ResidentialUnitDto> = emptyList()
) : ResidentialApi {
    var calls = 0
    var lastComplexId: String? = null

    override suspend fun getCensus(complexId: String): List<ResidentialUnitDto> {
        calls++
        lastComplexId = complexId
        return units
    }
}
