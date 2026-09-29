package co.edu.unicauca.urbanguard.domain.usecase.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Genera un pase QR temporal para un visitante.
 *
 * La validación de los datos (nombre no vacío, cantidad dentro de los topes y
 * fecha no anterior a hoy) se hace en la capa de UI; el caso de uso delega
 * tal cual en el repositorio.
 */
class GenerateVisitorQrUseCase @Inject constructor(
    private val repository: VisitorRepository
) {
    suspend operator fun invoke(
        visitorName: String,
        guestCount: Int,
        date: LocalDate,
        complexId: String
    ): Result<VisitorAuthorization> {
        return repository.generateVisitorQr(visitorName, guestCount, date, complexId)
    }
}
