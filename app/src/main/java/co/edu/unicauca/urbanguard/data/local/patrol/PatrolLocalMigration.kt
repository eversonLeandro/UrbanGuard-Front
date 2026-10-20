package co.edu.unicauca.urbanguard.data.local.patrol

import co.edu.unicauca.urbanguard.data.mapper.toRoundPointDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migra las referencias locales del formato viejo (`id` + `order`) al nuevo
 * (`checkpointId`). El punto viejo y el checkpoint se emparejan por UID.
 *
 * Se ejecuta cuando ya hay checkpoints descargados del backend. Las rutas y
 * rondas que apunten a puntos inexistentes se limpian; los ids que ya son
 * checkpoints se conservan.
 */
@Singleton
class PatrolLocalMigration @Inject constructor(
    private val pointStore: PatrolPointLocalStore,
    private val routeStore: PatrolRouteLocalStore,
    private val roundStore: PatrolRoundLocalStore
) {

    private val inProgress = mutableSetOf<String>()

    @Synchronized
    fun migrate(complexId: String) {
        if (complexId.isBlank() || !inProgress.add(complexId)) return
        try {
            val current = pointStore.load(complexId)
            val legacy = pointStore.loadLegacy(complexId)
            if (current.isEmpty() || legacy.isEmpty()) return

            val byUid = current.associateBy { normalizeNfcUid(it.currentTag?.uid.orEmpty()) }
            val oldToNew = legacy.mapNotNull { old ->
                byUid[normalizeNfcUid(old.nfcTagId)]?.let { old.id to it.checkpointId }
            }.toMap()
            val currentIds = current.mapTo(mutableSetOf()) { it.checkpointId }

            val routes = migrateRoutes(complexId, oldToNew, currentIds)
            migrateRounds(complexId, routes, current, oldToNew)
            pointStore.clearLegacy(complexId)
        } finally {
            inProgress.remove(complexId)
        }
    }

    private fun migrateRoutes(
        complexId: String,
        oldToNew: Map<String, String>,
        currentIds: Set<String>
    ): List<PatrolRouteDto> {
        val routes = routeStore.load(complexId)
        val migrated = routes.map { route ->
            route.copy(pointIds = route.pointIds.mapNotNull { remap(it, oldToNew, currentIds) })
        }
        if (migrated != routes) routeStore.save(complexId, migrated)
        return migrated
    }

    private fun migrateRounds(
        complexId: String,
        routes: List<PatrolRouteDto>,
        points: List<CheckpointResponseDto>,
        oldToNew: Map<String, String>
    ) {
        val currentIds = points.mapTo(mutableSetOf()) { it.checkpointId }
        val pointById = points.associateBy { it.checkpointId }
        val routeById = routes.associateBy { it.id }
        val rounds = roundStore.load(complexId)
        val migrated = rounds.map { round ->
            round.copy(
                scans = remapScans(round, oldToNew, currentIds),
                points = snapshotFor(round, routeById, pointById, oldToNew, currentIds)
            )
        }
        if (migrated != rounds) roundStore.save(complexId, migrated)
    }

    private fun remapScans(
        round: PatrolRoundDto,
        oldToNew: Map<String, String>,
        currentIds: Set<String>
    ) = round.scans.map { scan ->
        val newPointId = scan.pointId?.let { remap(it, oldToNew, currentIds) }
        scan.copy(pointId = newPointId, known = scan.known && newPointId != null)
    }

    private fun snapshotFor(
        round: PatrolRoundDto,
        routeById: Map<String, PatrolRouteDto>,
        pointById: Map<String, CheckpointResponseDto>,
        oldToNew: Map<String, String>,
        currentIds: Set<String>
    ) = if (!round.points.isNullOrEmpty()) {
        round.points.mapNotNull { point ->
            val newId = remap(point.id, oldToNew, currentIds) ?: return@mapNotNull null
            pointById[newId]?.toRoundPointDto() ?: point.copy(id = newId)
        }
    } else {
        routeById[round.routeId]?.pointIds
            ?.mapNotNull { pointById[it]?.toRoundPointDto() }
            .orEmpty()
    }

    private fun remap(
        id: String,
        oldToNew: Map<String, String>,
        currentIds: Set<String>
    ): String? = oldToNew[id] ?: id.takeIf { it in currentIds }
}
