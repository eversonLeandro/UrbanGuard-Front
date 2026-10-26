package co.edu.unicauca.urbanguard.data.remote.api.service

object PatrolApiPaths {

    private const val CHECKPOINT_COMPLEXES = "api/complexes/{complexId}"
    private const val SECURITY_COMPLEXES = "api/security/complexes/{complexId}"

    const val POINTS = "$CHECKPOINT_COMPLEXES/checkpoints"
    const val POINT = "$POINTS/{checkpointId}"
    const val TAG_ASSIGNMENTS = "$POINT/tag-assignments"

    const val ROUTES = "$CHECKPOINT_COMPLEXES/patrol-routes"
    const val ROUTE = "$ROUTES/{routeId}"

    const val OCCURRENCES = "$CHECKPOINT_COMPLEXES/patrol-occurrences"
    const val OCCURRENCES_AVAILABLE = "$OCCURRENCES/available"
    const val OCCURRENCES_BATCHES = "$OCCURRENCES/batches"
    const val OCCURRENCE_START = "$OCCURRENCES/{occurrenceId}/start"

    const val EXECUTIONS = "$CHECKPOINT_COMPLEXES/patrol-executions"
    const val EXECUTION_ACTIVE = "$EXECUTIONS/active"
    const val EXECUTIONS_MINE = "$EXECUTIONS/mine"
    const val EXECUTION_DETAIL = "$EXECUTIONS/{executionId}"
    const val EXECUTION_SCANS = "$EXECUTIONS/{executionId}/scans"
    const val EXECUTION_FINISH = "$EXECUTIONS/{executionId}/finish"

    // Rutas legacy mientras se completa migración total
    const val ROUNDS = "$CHECKPOINT_COMPLEXES/patrol-rounds"
    const val ROUND_SCANS = "$ROUNDS/{roundId}/scans"
    const val ROUND_FINISH = "$ROUNDS/{roundId}/finish"

    const val SHIFTS = "$SECURITY_COMPLEXES/patrol-shifts"
    const val SHIFT_DETAIL = "$SHIFTS/detail"
    const val GUARD_SUMMARY = "$SECURITY_COMPLEXES/patrol-guards/{guardId}/summary"
}
