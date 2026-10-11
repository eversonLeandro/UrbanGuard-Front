package co.edu.unicauca.urbanguard.domain.model

/**
 * Datos de la entrada de patrullaje del guardia: rutas de hoy, ronda en
 * curso si la hay y rondas recientes del conjunto.
 */
data class PatrolEntry(
    val todayRoutes: List<PatrolRoute> = emptyList(),
    val activeRound: PatrolRound? = null,
    val recentRounds: List<PatrolRound> = emptyList()
)
