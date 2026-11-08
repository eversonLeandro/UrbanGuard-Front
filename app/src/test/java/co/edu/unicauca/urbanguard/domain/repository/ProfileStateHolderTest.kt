package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MultipartBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileStateHolderTest {
    private val fixed = Instant.parse("2026-11-07T12:00:00Z")

    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun `first concurrent callers share one GET and reuse fresh photo`() = runTest {
        val session = session()
        val users = FakeUsers(profile("one", fixed.plusSeconds(300)))
        val holder = ProfileStateHolder(users, session).apply { now = { fixed } }
        val a = async { holder.ensureFresh() }
        val b = async { holder.ensureFresh() }
        advanceUntilIdle()
        assertEquals("one", a.await().getOrThrow().profilePhotoUrl)
        assertEquals("one", b.await().getOrThrow().profilePhotoUrl)
        assertEquals(1, users.getCalls)
        holder.ensureFresh()
        assertEquals(1, users.getCalls)
    }

    @Test fun `near expiry refreshes and PUT publishes immediately`() = runTest {
        val session = session()
        val users = FakeUsers(profile("old", fixed.plusSeconds(300)))
        var instant = fixed
        val holder = ProfileStateHolder(users, session).apply { now = { instant } }
        advanceUntilIdle()
        holder.ensureFresh()
        instant = fixed.plusSeconds(260)
        users.response = profile("renewed", instant.plusSeconds(300))
        holder.ensureFresh()
        assertEquals(2, users.getCalls)
        assertEquals("renewed", holder.profile.value?.profilePhotoUrl)
        holder.publish(profile("uploaded", instant.plusSeconds(300)), "user-1")
        assertEquals("uploaded", holder.profile.value?.profilePhotoUrl)
    }

    @Test fun `late GET cannot overwrite successful PUT`() = runTest {
        val session = session()
        val users = FakeUsers(profile("old", fixed.plusSeconds(300)))
        val holder = ProfileStateHolder(users, session).apply { now = { fixed } }
        advanceUntilIdle()
        users.pending = CompletableDeferred()
        val pendingGet = async { holder.ensureFresh() }
        advanceUntilIdle()
        holder.publish(profile("new", fixed.plusSeconds(300)), "user-1")
        users.pending!!.complete(profile("old", fixed.plusSeconds(300)))
        advanceUntilIdle()
        pendingGet.await()
        assertEquals("new", holder.currentProfile()?.profilePhotoUrl)
    }

    @Test fun `late PUT from previous user is rejected before and after session collection`() = runTest {
        val session = session()
        val users = FakeUsers(profile("a", fixed.plusSeconds(300)))
        val holder = ProfileStateHolder(users, session).apply { now = { fixed } }
        advanceUntilIdle()
        holder.ensureFresh()
        session.saveSession(UserSession("jwt2", "user-2", "Otra", UserRole.GUARD))
        assertNull(holder.currentProfile())
        holder.publish(profile("a-upload", fixed.plusSeconds(300)), "user-1")
        advanceUntilIdle()
        assertNull(holder.currentProfile())
        users.response = profile("b", fixed.plusSeconds(300))
        holder.ensureFresh()
        holder.publish(profile("a-upload", fixed.plusSeconds(300)), "user-1")
        assertEquals("b", holder.currentProfile()?.profilePhotoUrl)
    }

    @Test fun `session switch clears photo and prevents cross account reuse`() = runTest {
        val session = session()
        val users = FakeUsers(profile("private", fixed.plusSeconds(300)))
        val holder = ProfileStateHolder(users, session).apply { now = { fixed } }
        advanceUntilIdle()
        holder.ensureFresh()
        session.saveSession(UserSession("jwt2", "user-2", "Otra", UserRole.GUARD))
        advanceUntilIdle()
        assertNull(holder.profile.value)
        users.response = profile("other", fixed.plusSeconds(300))
        holder.ensureFresh()
        assertEquals("other", holder.profile.value?.profilePhotoUrl)
        session.clearSession()
        advanceUntilIdle()
        assertNull(holder.profile.value)
    }

    @Test fun `failed refresh removes expired URL`() = runTest {
        val session = session()
        val users = FakeUsers(profile("old", fixed.plusSeconds(50)))
        var instant = fixed
        val holder = ProfileStateHolder(users, session).apply { now = { instant } }
        holder.ensureFresh()
        instant = fixed.plusSeconds(60)
        users.failure = true
        holder.ensureFresh()
        assertNull(holder.profile.value?.profilePhotoUrl)
    }

    private fun session() = SessionManager(NoOpRolePersistence).apply {
        saveSession(UserSession("jwt", "user-1", "Ana", UserRole.RESIDENT))
    }
    private fun profile(url: String, expiry: Instant) = OwnProfile("id", "Ana", url, expiry)

    private class FakeUsers(var response: OwnProfile) : UserRepository {
        var getCalls = 0
        var failure = false
        var pending: CompletableDeferred<OwnProfile>? = null
        override suspend fun getOwnProfile(): Result<OwnProfile> {
            getCalls++
            return if (failure) Result.failure(IllegalStateException("red"))
            else Result.success(pending?.await() ?: response)
        }
        override suspend fun updateProfilePhoto(image: MultipartBody.Part, token: String) = Result.success(response)
        override suspend fun listUsers(estado: String): Result<List<User>> = Result.success(emptyList())
        override suspend fun deactivateUser(userId: String) = Result.success(Unit)
        override suspend fun reactivateUser(userId: String): Result<User> =
            Result.failure(UnsupportedOperationException())
        override suspend fun createPasswordChangeTicket(): Result<String> =
            Result.failure(UnsupportedOperationException())
    }
}
