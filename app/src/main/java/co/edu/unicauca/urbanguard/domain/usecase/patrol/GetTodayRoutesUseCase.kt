package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.forDay
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * Rutas con horario para hoy en el conjunto del guardia.
 *
 * Es lo que alimenta la tarjeta "Ronda de hoy" de la entrada de seguridad.
 */
class GetTodayRoutesUseCase @Inject constructor(
    private val repository: PatrolRouteRepository
) {
    suspend operator fun invoke(
        complexId: String,
        day: DayOfWeek = LocalDate.now().dayOfWeek
    ): Result<List<PatrolRoute>> {
        return repository.getRoutes(complexId).map { routes -> routes.forDay(day) }
    }
}
