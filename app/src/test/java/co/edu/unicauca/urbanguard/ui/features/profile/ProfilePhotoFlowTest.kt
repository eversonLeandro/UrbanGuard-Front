package co.edu.unicauca.urbanguard.ui.features.profile

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import co.edu.unicauca.urbanguard.core.media.ProfilePhotoPreparer
import co.edu.unicauca.urbanguard.core.media.PreparedProfilePhoto
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ProfileStateHolder
import co.edu.unicauca.urbanguard.domain.repository.ReauthLoginProvider
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import co.edu.unicauca.urbanguard.domain.usecase.users.ChangePasswordUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.MultipartBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfilePhotoFlowTest {
    @Before fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun `successful PUT updates shared profile and ignores second tap`() = runTest {
        val users = FakeUsers()
        val (vm, holder, _) = viewModel(users)
        advanceUntilIdle()
        val source = image()
        vm.updatePhoto(Uri.fromFile(source))
        vm.updatePhoto(Uri.fromFile(source))
        awaitUpload(vm)
        assertEquals(1, users.putCalls)
        assertEquals("https://new-photo", holder.profile.value?.profilePhotoUrl)
        assertEquals("https://new-photo", vm.uiState.value.profilePhotoUrl)
        assertFalse(vm.uiState.value.isPhotoUploading)
        source.delete()
    }

    @Test fun `timeout and conflict reconcile GET without retrying PUT`() = runTest {
        for (error in listOf(AppError.Timeout("timeout"), AppError.Conflict("conflict"))) {
            val users = FakeUsers().apply { putError = error }
            val (vm, holder, _) = viewModel(users)
            advanceUntilIdle()
            val source = image()
            vm.updatePhoto(Uri.fromFile(source))
            awaitUpload(vm)
            assertEquals(1, users.putCalls)
            assertTrue(users.getCalls >= 2)
            assertEquals("https://old-photo", holder.profile.value?.profilePhotoUrl)
            assertTrue(vm.uiState.value.photoError!!.contains("No pudimos confirmar"))
            source.delete()
        }
    }

    @Test fun `PUT started by prior account cannot publish into next account`() = runTest {
        val users = FakeUsers().apply { pendingPut = CompletableDeferred() }
        val (vm, holder, session) = viewModel(users)
        advanceUntilIdle()
        val source = image()
        vm.updatePhoto(Uri.fromFile(source))
        withContext(Dispatchers.Default) {
            withTimeout(10_000) { while (users.putCalls == 0) delay(10) }
        }
        users.getProfile = OwnProfile("id-B", "Bea", null, null)
        session.saveSession(UserSession("jwt2", "auth0|2", "Bea", UserRole.ADMIN))
        users.pendingPut!!.complete(OwnProfile("id-A", "Ana", "https://a-private", Instant.now().plusSeconds(300)))
        awaitUpload(vm)
        assertEquals("jwt", users.lastUploadToken)
        assertEquals(null, holder.currentProfile()?.profilePhotoUrl)
        assertEquals(null, vm.uiState.value.profilePhotoUrl)
        source.delete()
    }

    @Test fun `switching accounts during preparation cancels PUT before transport`() = runTest {
        val users = FakeUsers()
        val delayed = DelayedPreparer()
        val (vm, _, session) = viewModel(users, delayed)
        advanceUntilIdle()
        val source = image()
        val ready = ProfilePhotoPreparer(RuntimeEnvironment.getApplication())
            .prepare(Uri.fromFile(source)).getOrThrow()
        vm.updatePhoto(Uri.fromFile(source))
        session.saveSession(UserSession("jwt2", "auth0|2", "Bea", UserRole.ADMIN))
        delayed.complete(ready)
        awaitUpload(vm)
        assertEquals(0, users.putCalls)
        ready.cleanup()
        source.delete()
    }

    private suspend fun awaitUpload(vm: ProfileViewModel) {
        withContext(Dispatchers.Default) {
            withTimeout(10_000) {
                while (vm.uiState.value.isPhotoUploading) delay(10)
            }
        }
    }

    private fun viewModel(
        users: FakeUsers,
        preparer: ProfilePhotoPreparer = ProfilePhotoPreparer(RuntimeEnvironment.getApplication())
    ): Triple<ProfileViewModel, ProfileStateHolder, SessionManager> {
        val session = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("jwt", "auth0|1", "Ana", UserRole.RESIDENT))
        }
        val holder = ProfileStateHolder(users, session)
        val vm = ProfileViewModel(
            session, object : ReauthLoginProvider {
                override suspend fun login(email: String, password: String) =
                    throw UnsupportedOperationException()
            }, ChangePasswordUseCase(users), holder, users,
            preparer
        )
        return Triple(vm, holder, session)
    }

    private fun image(): File {
        val file = File.createTempFile("profile", ".jpg", RuntimeEnvironment.getApplication().cacheDir)
        val bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLUE)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        bitmap.recycle()
        return file
    }

    private class FakeUsers : UserRepository {
        var getCalls = 0
        var putCalls = 0
        var putError: AppError? = null
        var pendingPut: CompletableDeferred<OwnProfile>? = null
        var lastUploadToken: String? = null
        private val old = OwnProfile("id", "Ana", "https://old-photo", Instant.now().plusSeconds(300))
        var getProfile: OwnProfile = old
        override suspend fun getOwnProfile(): Result<OwnProfile> {
            getCalls++
            return Result.success(getProfile)
        }
        override suspend fun updateProfilePhoto(image: MultipartBody.Part, token: String): Result<OwnProfile> {
            putCalls++
            lastUploadToken = token
            pendingPut?.let { return Result.success(it.await()) }
            return putError?.let { Result.failure(AppException(it)) }
                ?: Result.success(old.copy(profilePhotoUrl = "https://new-photo"))
        }
        override suspend fun listUsers(estado: String): Result<List<User>> = Result.success(emptyList())
        override suspend fun deactivateUser(userId: String) = Result.success(Unit)
        override suspend fun reactivateUser(userId: String): Result<User> =
            Result.failure(UnsupportedOperationException())
        override suspend fun createPasswordChangeTicket(): Result<String> =
            Result.failure(UnsupportedOperationException())
    }

    private class DelayedPreparer : ProfilePhotoPreparer(RuntimeEnvironment.getApplication()) {
        private val result = CompletableDeferred<PreparedProfilePhoto>()
        override suspend fun prepare(uri: Uri): Result<PreparedProfilePhoto> = Result.success(result.await())
        fun complete(photo: PreparedProfilePhoto) { result.complete(photo) }
    }
}
