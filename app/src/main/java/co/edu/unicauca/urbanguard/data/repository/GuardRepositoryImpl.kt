package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toCreateGuardRequestDto
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.GuardApi
import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import javax.inject.Inject

class GuardRepositoryImpl @Inject constructor(
    private val guardApi: GuardApi
) : GuardRepository {

    override suspend fun registerGuard(
        complexId: String,
        email: String,
        nombres: String,
        apellidos: String,
        telefono: String
    ): Result<CreatedGuard> = apiCall {
        val request = toCreateGuardRequestDto(
            email = email,
            nombres = nombres,
            apellidos = apellidos,
            telefono = telefono
        )
        guardApi.createGuard(complexId, request).toDomain()
    }
}
