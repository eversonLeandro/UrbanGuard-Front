package co.edu.unicauca.urbanguard.data.remote.api.service

object PatrolApiPaths {

    private const val CHECKPOINT_COMPLEXES = "api/complexes/{complexId}"
    private const val SECURITY_COMPLEXES = "api/security/complexes/{complexId}"

    const val POINTS = "$CHECKPOINT_COMPLEXES/checkpoints"
    const val POINT = "$POINTS/{checkpointId}"
    const val TAG_ASSIGNMENTS = "$POINT/tag-assignments"

    const val ROUTES = "$SECURITY_COMPLEXES/patrol-routes"
    const val ROUTE = "$ROUTES/{routeId}"

    const val ROUNDS = "$SECURITY_COMPLEXES/patrol-rounds"
    const val ROUND_SCANS = "$ROUNDS/{roundId}/scans"
    const val ROUND_FINISH = "$ROUNDS/{roundId}/finish"

    const val SHIFTS = "$SECURITY_COMPLEXES/patrol-shifts"
    const val SHIFT_DETAIL = "$SHIFTS/detail"
    const val GUARD_SUMMARY = "$SECURITY_COMPLEXES/patrol-guards/{guardId}/summary"
}
