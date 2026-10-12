package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.repository.NotificationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los casos de uso son passthrough delgados: delegan al repositorio y
 * propagan el resultado intacto (el ViewModel nunca muestra errores de push,
 * pero el resultado existe para logging y tests).
 */
class DeviceTokenUseCasesTest {

    @Test
    fun `register forwards token device and conjunto`() = runTest {
        val fake = FakeNotificationRepository(Result.success(Unit))
        val useCase = RegisterDeviceTokenUseCase(fake)

        val result = useCase("fcm-1", "dev-1", "conj-1")

        assertTrue(result.isSuccess)
        assertEquals(Triple("fcm-1", "dev-1", "conj-1"), fake.lastRegister)
    }

    @Test
    fun `register propagates repository failures untouched`() = runTest {
        val failure = Result.failure<Unit>(RuntimeException("red caída"))
        val useCase = RegisterDeviceTokenUseCase(FakeNotificationRepository(failure))

        val result = useCase("fcm-1", null, "conj-1")

        assertTrue(result.isFailure)
        assertEquals(failure.exceptionOrNull(), result.exceptionOrNull())
    }

    @Test
    fun `unregister forwards token`() = runTest {
        val fake = FakeNotificationRepository(Result.success(Unit))
        val useCase = UnregisterDeviceTokenUseCase(fake)

        val result = useCase("fcm-1")

        assertTrue(result.isSuccess)
        assertEquals("fcm-1", fake.lastDeregister)
    }

    private class FakeNotificationRepository(
        private val fixed: Result<Unit>
    ) : NotificationRepository {
        var lastRegister: Triple<String, String?, String>? = null
        var lastDeregister: String? = null

        override suspend fun registerDeviceToken(
            fcmToken: String,
            deviceId: String?,
            conjuntoId: String
        ): Result<Unit> {
            lastRegister = Triple(fcmToken, deviceId, conjuntoId)
            return fixed
        }

        override suspend fun deregisterDeviceToken(fcmToken: String): Result<Unit> {
            lastDeregister = fcmToken
            return fixed
        }
    }
}
