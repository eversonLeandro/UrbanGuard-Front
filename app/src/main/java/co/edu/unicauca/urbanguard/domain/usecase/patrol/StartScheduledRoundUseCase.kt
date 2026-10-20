package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.coveringOccurrence
import co.edu.unicauca.urbanguard.domain.model.nextOccurrenceAfter
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Abre una ronda tomando la foto fija de puntos de la ruta y calculando la
 * cita que cubre y el siguiente vencimiento.
 */
class StartScheduledRoundUseCase @Inject constructor(
    private val getPatrolRoutesUseCase: GetPatrolRoutesUseCase,
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val startRoundUseCase: StartRoundUseCase
) {
    suspend operator fun invoke(
        complexId: String,
        route: PatrolRoute,
        now: LocalDateTime = LocalDateTime.now()
    ): PatrolRound {
        val zone = ZoneId.systemDefault()
        // Los puntos primero: al cargarlos se migran las referencias viejas
        // de las rutas locales. Después se relee la ruta ya migrada.
        val pointsById = getPatrolPointsUseCase(complexId).getOrThrow().associateBy { it.id }
        val routes = getPatrolRoutesUseCase(complexId).getOrThrow()
        val currentRoute = routes.firstOrNull { it.id == route.id } ?: route
        val occurrence = coveringOccurrence(
            currentRoute,
            now,
            PatrolConstants.ROUND_EARLY_START_MINUTES
        )
        val due = occurrence?.let { nextOccurrenceAfter(routes, it) }
        return startRoundUseCase(
            complexId = complexId,
            routeId = currentRoute.id,
            routeName = currentRoute.name,
            points = currentRoute.pointIds.mapNotNull(pointsById::get),
            scheduledAt = occurrence?.atZone(zone)?.toInstant(),
            dueAt = due?.atZone(zone)?.toInstant()
        )
    }
}
