package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.LoginRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.AuthApi
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val response = authApi.login(LoginRequestDto(email, password))
            val session = response.toDomain()
            sessionManager.saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))   // 🔄 antes: 3 catch por separado
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }
}