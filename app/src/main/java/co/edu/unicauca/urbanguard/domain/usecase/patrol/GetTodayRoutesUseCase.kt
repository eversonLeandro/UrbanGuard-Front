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
 * Rutas con horario para hoy en el conjunto del guardia.
 *
 * Es lo que alimenta la tarjeta "Ronda de hoy" de la entrada de seguridad.
 * En modo remoto, consulta las ocurrencias del backend para hoy y el día
 * siguiente (para cubrir rondas de madrugada que cruzan la medianoche).
 * Se pasan parámetros `from`/`to` explícitos para que el backend devuelva
 * todas las ocurrencias del rango sin filtrar por estado.
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
            val remoteResult: Result<List<PatrolRoute>> = runCatching {
                // Pedir desde ayer hasta pasado mañana para cubrir:
                // - Rondas de madrugada cuyo scheduledAt UTC cae el día «anterior» en la BD
                // - Desfases de zona horaria entre el dispositivo y el backend (America/Bogota)
                val today = LocalDate.now(BACKEND_ZONE)
                val from = today.toString()
                val to   = today.toString()
                val occurrences = executionApi.getAvailableOccurrences(
                    complexId = complexId,
                    from = from,
                    to   = to
                )
                // Filtrar solo SCHEDULED e IN_PROGRESS en el cliente para no mostrar
                // rondas ya vencidas (NOT_PERFORMED) ni las ya completadas (COMPLETED)
                val active = occurrences.filter { occ ->
                    occ.status == "SCHEDULED" || occ.status == "IN_PROGRESS"
                }

                // Filtrar solo las ocurrencias cuya fecha programada en Bogotá corresponda a hoy
                val todayOccurrences = active.filter { occ ->
                    val inst = runCatching { java.time.Instant.parse(occ.scheduledAt) }.getOrNull()
                    if (inst == null) false
                    else {
                        val ldt = inst.atZone(BACKEND_ZONE).toLocalDateTime()
                        ldt.toLocalDate() == today
                    }
                }

                // Cada ocurrencia se convierte en su propia tarjeta independiente.
                // Ordenar por scheduledAt para que aparezcan cronológicamente.
                val sorted = todayOccurrences.sortedBy { it.scheduledAt }

                // Obtener las rutas reales para extraer sus pointIds (checkpoints).
                val realRoutes = runCatching {
                    repository.getRoutes(complexId).getOrDefault(emptyList())
                }.getOrDefault(emptyList())
                val pointsById = realRoutes.associateBy { it.id }

                sorted.map { occ ->
                    val baseRoute = occ.toRoute(complexId)
                    val realPointIds = pointsById[occ.routeId]?.pointIds.orEmpty()
                    val pointIds = if (realPointIds.isNotEmpty()) {
                        realPointIds
                    } else {
                        List(occ.pointsExpected) { "point-$it" }
                    }
                    baseRoute.copy(pointIds = pointIds)
                }
            }.recoverCatching {
                repository.getRoutes(complexId).getOrDefault(emptyList()).forDay(day)
            }
            return remoteResult
        }
        return repository.getRoutes(complexId).map { routes -> routes.forDay(day) }
    }

}
