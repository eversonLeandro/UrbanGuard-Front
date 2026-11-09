package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import java.util.Locale

enum class CensusUnitFilter(val label: String) {
    ALL("Todos"),
    OCCUPIED("Asociados"),
    UNOCCUPIED("Sin asociar")
}

data class CensusUiState(
    val isLoading: Boolean = false,
    val units: List<ResidentialUnit> = emptyList(),
    val error: AppError? = null,
    val searchQuery: String = "",
    val filter: CensusUnitFilter = CensusUnitFilter.ALL,
    val selectedUnitId: String? = null,
    val isAdmin: Boolean = false,
    val isRegistering: Boolean = false,
    val registrationSuccess: String? = null,
    val registrationError: AppError? = null,
    val isEditingUnit: Boolean = false,
    val isUpdatingUnit: Boolean = false,
    val updateUnitSuccess: String? = null,
    val updateUnitError: AppError? = null,
    val isChangingResidentStatus: Boolean = false,
    val changingResidentId: String? = null,
    val residentStatusSuccess: String? = null,
    val residentStatusError: AppError? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && error == null && units.isEmpty()

    val occupiedUnits: Int
        get() = units.count { it.residents.isNotEmpty() }

    val unoccupiedUnits: Int
        get() = units.size - occupiedUnits

    val selectedUnit: ResidentialUnit?
        get() = selectedUnitId?.let { id -> units.firstOrNull { it.id == id } }

    fun visibleUnits(): List<ResidentialUnit> {
        val normalizedQuery = normalize(searchQuery)
        return units.asSequence()
            .filter { unit ->
                when (filter) {
                    CensusUnitFilter.ALL -> true
                    CensusUnitFilter.OCCUPIED -> unit.residents.isNotEmpty()
                    CensusUnitFilter.UNOCCUPIED -> unit.residents.isEmpty()
                }
            }
            .filter { unit ->
                normalizedQuery.isEmpty() ||
                    normalize(unit.identifier).contains(normalizedQuery) ||
                    unit.residents.any { normalize(it.fullName).contains(normalizedQuery) }
            }
            .sortedWith { first, second ->
                compareNatural(first.identifier, second.identifier)
                    .takeIf { it != 0 }
                    ?: first.id.compareTo(second.id)
            }
            .toList()
    }

    private fun normalize(value: String): String =
        value.trim().replace(WHITESPACE, " ").lowercase(Locale.ROOT)

    private fun compareNatural(first: String, second: String): Int {
        var firstIndex = 0
        var secondIndex = 0

        while (firstIndex < first.length && secondIndex < second.length) {
            val firstIsDigit = first[firstIndex].isDigit()
            val secondIsDigit = second[secondIndex].isDigit()
            if (firstIsDigit && secondIsDigit) {
                var firstEnd = firstIndex
                while (firstEnd < first.length && first[firstEnd].isDigit()) firstEnd++
                var secondEnd = secondIndex
                while (secondEnd < second.length && second[secondEnd].isDigit()) secondEnd++

                val firstNumber = first.substring(firstIndex, firstEnd).trimStart('0')
                val secondNumber = second.substring(secondIndex, secondEnd).trimStart('0')
                val numericComparison = firstNumber.length.compareTo(secondNumber.length)
                    .takeIf { it != 0 }
                    ?: firstNumber.compareTo(secondNumber)
                if (numericComparison != 0) return numericComparison
                firstIndex = firstEnd
                secondIndex = secondEnd
            } else {
                val characterComparison = first[firstIndex].lowercaseChar()
                    .compareTo(second[secondIndex].lowercaseChar())
                if (characterComparison != 0) return characterComparison
                firstIndex++
                secondIndex++
            }
        }

        return (first.length - firstIndex).compareTo(second.length - secondIndex)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
