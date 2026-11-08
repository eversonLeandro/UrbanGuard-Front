package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.UserApi
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import okhttp3.MultipartBody
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Named

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    @Named("profileUpload") private val uploadApi: UserApi = userApi
) : UserRepository {

    override suspend fun getOwnProfile(): Result<OwnProfile> = apiCall {
        userApi.getOwnProfile().toDomain()
    }

    override suspend fun updateProfilePhoto(image: MultipartBody.Part, token: String): Result<OwnProfile> = apiCall {
        uploadApi.updateProfilePhoto("Bearer $token", image).toDomain()
    }

    /**
     * Delega al endpoint POST /api/users/me/password-change del backend.
     * La URL sensible viaja en el campo "url" de la respuesta; nunca se persiste.
     * El mapeo de errores (REAUTH_REQUIRED, 422, 429, etc.) corre a cargo de
     * [co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper] via [apiCall].
     */
    override suspend fun createPasswordChangeTicket(): Result<String> = apiCall {
        userApi.createPasswordChangeTicket().url
    }

    override suspend fun listUsers(estado: String): Result<List<User>> = apiCall {
        userApi.listUsers(estado).map { it.toDomain() }
    }

    /** 204 vació: Retrofit resuelve la suspensión sin cuerpo que mapear. */
    override suspend fun deactivateUser(userId: String): Result<Unit> = apiCall {
        userApi.deactivateUser(userId)
        Unit
    }

    override suspend fun reactivateUser(userId: String): Result<User> = apiCall {
        userApi.reactivateUser(userId).toDomain()
    }
}
