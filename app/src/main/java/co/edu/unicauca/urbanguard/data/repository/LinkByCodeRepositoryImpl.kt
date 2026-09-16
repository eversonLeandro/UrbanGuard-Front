package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.LinkByCodeApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkByCodeRepository
import javax.inject.Inject

class LinkByCodeRepositoryImpl @Inject constructor(
    private val api: LinkByCodeApi
) : LinkByCodeRepository {

    override suspend fun searchConjunto(code: String): Result<ConjuntoSearch> {
        // TODO: reemplazar por llamada real cuando el backend esté listo:
        // return try {
        //     Result.success(api.searchConjunto(code).toDomain())
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        return if (code == "URB-4X9K") {
            Result.success(
                ConjuntoSearch(
                    id = "conjunto-001",
                    nombre = "Conjunto El Pinar",
                    direccion = "Calle 45 #23-15",
                    ciudad = "Bogotá, Colombia",
                    totalMiembros = 7
                )
            )
        } else {
            Result.failure(Exception("No se encontró ningún conjunto con ese código"))
        }
    }

    override suspend fun requestLink(conjuntoId: String, unidad: String): Result<LinkRequest> {
        // TODO: reemplazar por llamada real cuando el backend esté listo:
        // return try {
        //     Result.success(api.requestLink(conjuntoId, unidad).toDomain())
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        return Result.success(
            LinkRequest(
                conjuntoId = conjuntoId,
                fechaSolicitud = "28 Ene 2024, 10:32 am",
                unidad = unidad,
                estado = "PENDIENTE"
            )
        )
    }
}