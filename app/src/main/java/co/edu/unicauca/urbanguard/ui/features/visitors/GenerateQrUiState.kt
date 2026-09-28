package co.edu.unicauca.urbanguard.ui.features.visitors

import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import java.time.LocalDate

/**
 * Estado del formulario de creación de un pase QR.
 *
 * El pase ya no vive aquí: cuando la creación termina bien el ViewModel emite
 * `GenerateQrEvent.PassGenerated` y la pantalla navega a la vista del pase,
 * que lo lee del repositorio. Este estado es solo el formulario en curso.
 */
data class GenerateQrUiState(
    val isLoading: Boolean = false,
    val visitorName: String = "",
    val guestCount: Int = VisitorConstants.MIN_GUESTS,
    val date: LocalDate = LocalDate.now(),
    val errorMessage: String? = null
)
