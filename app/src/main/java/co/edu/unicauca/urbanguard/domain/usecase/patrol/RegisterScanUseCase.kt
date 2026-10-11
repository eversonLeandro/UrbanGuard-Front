package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/**
 * Registra una lectura en la ronda abierta.
 *
 * Devuelve la ronda actualizada y el punto coincidente (null si el tag no
 * pertenece a ninguna ruta del conjunto).
 */
class RegisterScanUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(
        complexId: String,
        tagId: String
    ): Result<Pair<PatrolRound, PatrolControlPoint?>> {
        return repository.registerScan(complexId, tagId)
    }
}
