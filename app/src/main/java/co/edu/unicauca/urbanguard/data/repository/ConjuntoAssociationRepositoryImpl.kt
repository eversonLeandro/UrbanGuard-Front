package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.CommunityApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoAssociationRepository
import javax.inject.Inject

/**
 * Implementación de [ConjuntoAssociationRepository] que consume el endpoint
 * GET /api/complexes del Community Service vía API Gateway.
 *
 * Todos los errores de red (HTTP, timeout, IOException) se traducen a mensajes
 * legibles para el usuario usando [NetworkErrorMapper], siguiendo el mismo patrón
 * que el resto de repositorios de la app.
 */
class ConjuntoAssociationRepositoryImpl @Inject constructor(
    private val api: CommunityApi
) : ConjuntoAssociationRepository {

    override suspend fun getMyConjuntos(): Result<List<ConjuntoUserAssociation>> {
        Log.d("ConjuntoRepo", "→ getMyConjuntos()")
        return try {
            val dtos = api.getMyComplexes()
            Log.d("ConjuntoRepo", "← ${dtos.size} DTOs recibidos")
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Log.e("ConjuntoRepo", "Error: ${e.message}", e)
            Result.failure(NetworkErrorMapper.toAppException(e))
        }
    }
}
