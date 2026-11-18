package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.data.mapper.toRoute
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.forDay
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Ocurrencias programadas para el día actual.
 *
 * Alimenta las rondas programadas de la entrada de seguridad. En modo remoto,
 * consulta las ocurrencias del backend en su zona horaria y conserva el
 * identificador/ventana de cada ocurrencia.
 */
class GetTodayRoutesUseCase @Inject constructor(
    private val repository: PatrolRouteRepository,
    private val executionApi: PatrolExecutionApi
) {
    companion object {
        /** Zona horaria del backend. */
        private val BACKEND_ZONE = ZoneId.of("America/Bogota")
    }

    suspend operator fun invoke(
        complexId: String,
        day: DayOfWeek = LocalDate.now().dayOfWeek
    ): Result<List<PatrolRoute>> {
        if (PatrolConstants.PATROL_ROUNDS_REMOTE) {
            val result = runCatching {
                val today = LocalDate.now(BACKEND_ZONE)
                val occurrences = executionApi.getAvailableOccurrences(
                    complexId = complexId,
                    from = today.toString(),
                    to = today.toString()
                )
                occurrences.asSequence()
                    .filter { it.status == "SCHEDULED" || it.status == "IN_PROGRESS" }
                    .sortedBy { it.scheduledAt }
                    .map { it.toRoute(complexId) }
                    .toList()
            }
            return result
        }
        return repository.getRoutes(complexId).map { routes -> routes.forDay(day) }
    }

}
