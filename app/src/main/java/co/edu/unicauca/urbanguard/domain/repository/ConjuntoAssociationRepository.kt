package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation

/**
 * Contrato de dominio para obtener las asociaciones (membresías) del usuario
 * autenticado en conjuntos residenciales.
 *
 * La implementación ([co.edu.unicauca.urbanguard.data.repository.ConjuntoAssociationRepositoryImpl])
 * consume el endpoint GET /api/complexes del Community Service.
 */
interface ConjuntoAssociationRepository {
    /**
     * Retorna la lista de conjuntos a los que el usuario pertenece,
     * con su rol en cada uno (ADMIN, RESIDENT o GUARD).
     *
     * @return [Result.success] con la lista (puede ser vacía si el usuario
     *         no tiene membresías) o [Result.failure] con un mensaje legible
     *         mapeado por [co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper].
     */
    suspend fun getMyConjuntos(): Result<List<ConjuntoUserAssociation>>
}
