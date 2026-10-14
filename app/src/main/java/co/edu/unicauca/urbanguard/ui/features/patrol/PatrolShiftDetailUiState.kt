package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import java.time.LocalDate

/**
 * Estado del detalle de un turno (HU-032): el veredicto de meta y sus rondas
 * llegan del backend; aquí solo se guarda qué turno se está mostrando y su
 * estado funcional de carga/error.
 */
data class PatrolShiftDetailUiState(
    val isLoading: Boolean = false,
    val detail: PatrolShiftDetail? = null,
    val errorMessage: String? = null,
    val shiftType: PatrolShiftType,
    val date: LocalDate
)
