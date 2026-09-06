package co.edu.unicauca.urbanguard.core.session

import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        sessionManager = SessionManager()
    }

    @Test
    fun initialSession_isNull() {
        assertNull(sessionManager.session.value)
        assertNull(sessionManager.token)
        assertNull(sessionManager.currentRole())
    }

    @Test
    fun saveSession_persistsSessionAndRole() {
        val userSession = UserSession(
            token = "jwt_token_sample",
            userId = "auth0|123456",
            name = "Admin User",
            role = UserRole.ADMIN
        )

        sessionManager.saveSession(userSession)

        assertEquals("jwt_token_sample", sessionManager.token)
        assertEquals(userSession, sessionManager.session.value)
        assertEquals(UserRole.ADMIN, sessionManager.currentRole())
    }

    @Test
    fun clearSession_resetsAllSessionData() {
        val userSession = UserSession(
            token = "jwt_token_sample",
            userId = "auth0|123456",
            name = "Guard User",
            role = UserRole.GUARD
        )

        sessionManager.saveSession(userSession)
        sessionManager.clearSession()

        assertNull(sessionManager.token)
        assertNull(sessionManager.session.value)
        assertNull(sessionManager.currentRole())
    }
}
