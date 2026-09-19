package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import android.util.Log
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoAssociationRepository
import javax.inject.Inject

/**
 * Obtiene la lista de conjuntos residenciales a los que el usuario
 * autenticado pertenece (membresías activas con su rol: ADMIN, RESIDENT, GUARD).
 *
 * Delega en [ConjuntoAssociationRepository]; no contiene lógica adicional
 * ya que el mapping y manejo de errores ocurren en la capa de datos.
 */
class GetMyConjuntosUseCase @Inject constructor(
    private val repository: ConjuntoAssociationRepository
) {
    suspend operator fun invoke(): Result<List<ConjuntoUserAssociation>> {
        Log.d("GetMyConjuntosUC", "Invocado")
        return repository.getMyConjuntos()
    }
}
