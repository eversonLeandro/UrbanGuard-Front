package co.edu.unicauca.urbanguard

import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.repository.ProfileStateHolder
import co.edu.unicauca.urbanguard.domain.repository.UserRepository

class NoopProfileUserRepository : UserRepository {
    override suspend fun getOwnProfile() =
        Result.failure<co.edu.unicauca.urbanguard.domain.model.OwnProfile>(UnsupportedOperationException())
    override suspend fun updateProfilePhoto(image: okhttp3.MultipartBody.Part, token: String) =
        Result.failure<co.edu.unicauca.urbanguard.domain.model.OwnProfile>(UnsupportedOperationException())
    override suspend fun listUsers(estado: String): Result<List<User>> = Result.success(emptyList())
    override suspend fun deactivateUser(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun reactivateUser(userId: String): Result<User> = Result.failure(UnsupportedOperationException())
    override suspend fun createPasswordChangeTicket(): Result<String> = Result.failure(UnsupportedOperationException())
}

fun testProfileState(session: SessionManager) = ProfileStateHolder(NoopProfileUserRepository(), session)
