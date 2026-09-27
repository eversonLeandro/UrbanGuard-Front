package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateLinkRequestRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.LinkByCodeApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkByCodeRepository
import javax.inject.Inject

class LinkByCodeRepositoryImpl @Inject constructor(
    private val api: LinkByCodeApi
) : LinkByCodeRepository {

    override suspend fun searchConjunto(code: String): Result<ConjuntoSearch> = apiCall {
        api.searchConjunto(code.trim()).toDomain()
    }

    override suspend fun requestLink(conjuntoId: String, unitId: String): Result<LinkRequest> =
        apiCall {
            api.requestLink(
                CreateLinkRequestRequestDto(complexId = conjuntoId, unitId = unitId)
            ).toDomain()
        }
}
