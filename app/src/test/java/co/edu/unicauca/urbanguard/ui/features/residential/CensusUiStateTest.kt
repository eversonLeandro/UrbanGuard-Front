package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CensusUiStateTest {

    @Test
    fun `visibleUnits ordena numeros naturalmente y calcula estadisticas globales`() {
        val state = CensusUiState(units = sampleUnits())

        assertEquals(listOf("1", "2", "10", "20", "100"), state.visibleUnits().map { it.identifier })
        assertEquals(5, state.units.size)
        assertEquals(3, state.occupiedUnits)
        assertEquals(2, state.unoccupiedUnits)
    }

    @Test
    fun `visibleUnits busca sin distinguir mayusculas y normaliza espacios`() {
        val state = CensusUiState(
            units = sampleUnits(),
            searchQuery = "  ANA   péREZ  "
        )

        assertEquals(listOf("10"), state.visibleUnits().map { it.identifier })
    }

    @Test
    fun `visibleUnits combina busqueda por numero con filtro de ocupacion`() {
        val occupied = CensusUiState(
            units = sampleUnits(),
            searchQuery = "2",
            filter = CensusUnitFilter.OCCUPIED
        )
        val unoccupied = occupied.copy(filter = CensusUnitFilter.UNOCCUPIED)

        assertEquals(listOf("20"), occupied.visibleUnits().map { it.identifier })
        assertEquals(listOf("2"), unoccupied.visibleUnits().map { it.identifier })
    }

    @Test
    fun `visibleUnits permite encontrar residentes y devuelve vacio sin coincidencias`() {
        val byResident = CensusUiState(
            units = sampleUnits(),
            searchQuery = "carlos"
        )
        val noMatch = byResident.copy(searchQuery = "inexistente")

        assertEquals(listOf("1"), byResident.visibleUnits().map { it.identifier })
        assertTrue(noMatch.visibleUnits().isEmpty())
    }

    @Test
    fun `visibleUnits ordena naturalmente identificadores alfanumericos`() {
        val state = CensusUiState(
            units = listOf(
                ResidentialUnit("unit-a10", "A10", emptyList()),
                ResidentialUnit("unit-b1", "B1", emptyList()),
                ResidentialUnit("unit-a2", "A2", emptyList())
            )
        )

        assertEquals(listOf("A2", "A10", "B1"), state.visibleUnits().map { it.identifier })
    }

    @Test
    fun `visibleUnits conserva conteos y resultados hasta 500 apartamentos`() {
        listOf(10, 100, 250, 500).forEach { size ->
            val units = (1..size).map { number ->
                ResidentialUnit(
                    id = "unit-$number",
                    identifier = number.toString(),
                    residents = if (number % 2 == 0) {
                        listOf(ResidentialResident(userId = "resident-$number", fullName = "Residente $number"))
                    } else {
                        emptyList()
                    }
                )
            }
            val state = CensusUiState(units = units)

            assertEquals(size, state.visibleUnits().size)
            assertEquals(size / 2, state.occupiedUnits)
            assertEquals(size - size / 2, state.unoccupiedUnits)
        }
    }

    private fun sampleUnits() = listOf(
        ResidentialUnit(
            "unit-10",
            "10",
            listOf(ResidentialResident(userId = "resident-ana", fullName = "Ana Pérez"))
        ),
        ResidentialUnit("unit-2", "2", emptyList()),
        ResidentialUnit(
            "unit-1",
            "1",
            listOf(ResidentialResident(userId = "resident-carlos", fullName = "Carlos Ruiz"))
        ),
        ResidentialUnit("unit-100", "100", emptyList()),
        ResidentialUnit(
            "unit-20",
            "20",
            listOf(ResidentialResident(userId = "resident-luz", fullName = "Luz Díaz"))
        )
    )
}
