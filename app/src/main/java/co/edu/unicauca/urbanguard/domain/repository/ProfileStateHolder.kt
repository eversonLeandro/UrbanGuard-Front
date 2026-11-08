package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/** Perfil efímero compartido. Nunca persiste la URL firmada. */
@Singleton
class ProfileStateHolder @Inject constructor(
    private val users: UserRepository,
    private val sessions: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private val _profile = MutableStateFlow<OwnProfile?>(null)
    val profile: StateFlow<OwnProfile?> = _profile
    @Volatile private var owner: String? = null
    private var fetchedAt: Instant? = null
    private var failedImageUrl: String? = null
    private val generation = AtomicLong(0)
    internal var now: () -> Instant = Instant::now

    /** Devuelve foto solo cuando pertenece a la sesión que está activa ahora. */
    fun currentProfile(): OwnProfile? = _profile.value.takeIf {
        owner != null && owner == sessions.session.value?.userId
    }

    init {
        scope.launch {
            sessions.session.collect { session ->
                val next = session?.userId
                if (next != owner) {
                    generation.incrementAndGet()
                    owner = next
                    fetchedAt = null
                    failedImageUrl = null
                    _profile.value = null
                }
            }
        }
    }

    suspend fun ensureFresh(force: Boolean = false): Result<OwnProfile> = mutex.withLock {
        val userId = sessions.session.value?.userId
            ?: return@withLock Result.failure(IllegalStateException("Sesión no disponible"))
        if (owner != userId) {
            generation.incrementAndGet()
            owner = userId
            fetchedAt = null
            failedImageUrl = null
            _profile.value = null
        }
        val current = _profile.value
        val instant = now()
        if (!force && current != null && !needsRefresh(current, fetchedAt, instant)) {
            return@withLock Result.success(current)
        }
        val startedAtGeneration = generation.get()
        val result = users.getOwnProfile()
        if (sessions.session.value?.userId == userId && generation.get() == startedAtGeneration) {
            result.onSuccess {
                _profile.value = it
                fetchedAt = instant
            }.onFailure {
                if (current?.profilePhotoExpiresAt?.isAfter(instant) != true) {
                    _profile.value = current?.copy(profilePhotoUrl = null, profilePhotoExpiresAt = null)
                }
            }
        } else {
            return@withLock _profile.value?.let(Result.Companion::success) ?: result
        }
        result
    }

    fun publish(updated: OwnProfile, expectedUserId: String) {
        if (sessions.session.value?.userId == expectedUserId && owner == expectedUserId) {
            generation.incrementAndGet()
            _profile.value = updated
            fetchedAt = now()
            failedImageUrl = null
        }
    }

    suspend fun refreshAfterImageError(url: String) {
        if (_profile.value?.profilePhotoUrl != url || failedImageUrl == url) return
        failedImageUrl = url
        ensureFresh(force = true)
    }

    companion object {
        private const val REFRESH_MARGIN_SECONDS = 45L
        private const val NO_PHOTO_TTL_SECONDS = 300L

        internal fun needsRefresh(profile: OwnProfile, fetchedAt: Instant?, now: Instant): Boolean {
            val expiry = profile.profilePhotoExpiresAt
            return if (profile.profilePhotoUrl != null) {
                expiry == null || !expiry.isAfter(now.plusSeconds(REFRESH_MARGIN_SECONDS))
            } else {
                fetchedAt == null || !fetchedAt.plusSeconds(NO_PHOTO_TTL_SECONDS).isAfter(now)
            }
        }
    }
}
