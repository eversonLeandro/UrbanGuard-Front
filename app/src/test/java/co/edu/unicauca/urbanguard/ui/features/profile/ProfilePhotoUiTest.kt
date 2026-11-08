package co.edu.unicauca.urbanguard.ui.features.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import co.edu.unicauca.urbanguard.core.media.ProfilePhotoPreparer
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ProfileStateHolder
import co.edu.unicauca.urbanguard.domain.repository.ReauthLoginProvider
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import co.edu.unicauca.urbanguard.domain.usecase.users.ChangePasswordUseCase
import co.edu.unicauca.urbanguard.ui.features.admin.SuperAdminScreen
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProfilePhotoUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `avatar opens approved camera and gallery dialog`() {
        val session = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("jwt", "auth0|1", "Ana", UserRole.RESIDENT))
        }
        val users = FakeUsers()
        val vm = ProfileViewModel(
            session,
            object : ReauthLoginProvider {
                override suspend fun login(email: String, password: String): UserSession =
                    throw UnsupportedOperationException()
            },
            ChangePasswordUseCase(users),
            ProfileStateHolder(users, session),
            users,
            ProfilePhotoPreparer(RuntimeEnvironment.getApplication())
        )
        compose.setContent { ProfileScreen(viewModel = vm) }
        compose.onNodeWithContentDescription("Agregar foto de perfil").performClick()
        compose.onNodeWithText("Tomar foto").assertExists()
        compose.onNodeWithText("Elegir de la galería").assertExists()
    }

    @Test fun `superadmin console opens the existing profile route`() {
        var opened = 0
        compose.setContent { SuperAdminScreen(onOpenProfile = { opened++ }) }
        compose.onNodeWithText("Mi perfil").performClick()
        assertEquals(1, opened)
    }

    private class FakeUsers : UserRepository {
        override suspend fun getOwnProfile() = Result.success(OwnProfile("id", "Ana", null, null))
        override suspend fun updateProfilePhoto(image: MultipartBody.Part, token: String) =
            Result.failure<OwnProfile>(UnsupportedOperationException())
        override suspend fun listUsers(estado: String): Result<List<User>> = Result.success(emptyList())
        override suspend fun deactivateUser(userId: String) = Result.success(Unit)
        override suspend fun reactivateUser(userId: String): Result<User> =
            Result.failure(UnsupportedOperationException())
        override suspend fun createPasswordChangeTicket(): Result<String> =
            Result.failure(UnsupportedOperationException())
    }
}
