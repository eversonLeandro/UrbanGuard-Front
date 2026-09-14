package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.LinkRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ReviewLinkRequestRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.LinkRequestApi
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import javax.inject.Inject

class LinkRequestRepositoryImpl @Inject constructor(
    private val api: LinkRequestApi
) : LinkRequestRepository {

    // TODO: reemplazar por llamadas reales al backend cuando esté listo.
    // Por ahora se devuelven datos de prueba para habilitar la UI (UG-49).

    private val fakeRequests = listOf(
        LinkRequest(
            id = "req-001",
            nombreSolicitante = "María Rodríguez",
            emailSolicitante = "maria.rodriguez@email.com",
            conjuntoId = "conjunto-001",
            conjuntoNombre = "Conjunto El Pinar",
            fechaSolicitud = "28 Ene 2024, 10:32 am",
            codigoVinculacion = "URB-4X9K",
            unidad = "Apartamento 302",
            estado = "PENDIENTE"
        ),
        LinkRequest(
            id = "req-002",
            nombreSolicitante = "Carlos Gómez",
            emailSolicitante = "carlos.gomez@email.com",
            conjuntoId = "conjunto-001",
            conjuntoNombre = "Conjunto El Pinar",
            fechaSolicitud = "27 Ene 2024, 4:15 pm",
            codigoVinculacion = "URB-4X9K",
            unidad = "Casa 12",
            estado = "PENDIENTE"
        ),
        LinkRequest(
            id = "req-003",
            nombreSolicitante = "Ana Martínez",
            emailSolicitante = "ana.martinez@email.com",
            conjuntoId = "conjunto-001",
            conjuntoNombre = "Conjunto El Pinar",
            fechaSolicitud = "26 Ene 2024, 9:05 am",
            codigoVinculacion = "URB-4X9K",
            unidad = "Apartamento 105",
            estado = "APROBADA"
        ),
        LinkRequest(
            id = "req-004",
            nombreSolicitante = "Pedro Ramírez",
            emailSolicitante = "pedro.ramirez@email.com",
            conjuntoId = "conjunto-001",
            conjuntoNombre = "Conjunto El Pinar",
            fechaSolicitud = "25 Ene 2024, 11:40 am",
            codigoVinculacion = "URB-4X9K",
            unidad = "Casa 8",
            estado = "RECHAZADA"
        )
    )

    override suspend fun getRequests(): Result<List<LinkRequest>> {
        // TODO: reemplazar por llamada real cuando el backend esté listo:
        // return try {
        //     Result.success(api.getLinkRequests().map { it.toDomain() })
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        return Result.success(fakeRequests)
    }

    override suspend fun getRequestById(id: String): Result<LinkRequest> {
        // TODO: reemplazar por llamada real cuando el backend esté listo:
        // return try {
        //     Result.success(api.getLinkRequestById(id).toDomain())
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        val request = fakeRequests.firstOrNull { it.id == id }
        return if (request != null) {
            Result.success(request)
        } else {
            Result.failure(Exception("No se encontró la solicitud solicitada"))
        }
    }

    override suspend fun reviewRequest(id: String, approve: Boolean): Result<LinkRequest> {
        // TODO: reemplazar por llamada real cuando el backend esté listo:
        // return try {
        //     val dto = api.reviewLinkRequest(
        //         id = id,
        //         request = ReviewLinkRequestRequestDto(approve = approve)
        //     )
        //     Result.success(dto.toDomain())
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        val request = fakeRequests.firstOrNull { it.id == id }
        if (request == null) {
            return Result.failure(Exception("No se encontró la solicitud solicitada"))
        }
        return Result.success(
            request.copy(estado = if (approve) "APROBADA" else "RECHAZADA")
        )
    }
}