package co.edu.unicauca.urbanguard.core.session

import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
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

    @Test
    fun setAssociations_updatesListAndResetsActiveId() {
        // Dado: sesión activa con un conjunto activo previo
        val userSession = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "Test User",
            role = UserRole.USER,
            conjuntosAsociados = listOf(
                ConjuntoUserAssociation("old-1", "Antiguo Conjunto", UserRole.RESIDENT)
            ),
            conjuntoActivoId = "old-1"
        )
        sessionManager.saveSession(userSession)

        // Cuando: llega la lista real del backend
        val newList = listOf(
            ConjuntoUserAssociation("real-1", "Torres del Prado", UserRole.ADMIN),
            ConjuntoUserAssociation("real-2", "Conjunto El Pinar", UserRole.RESIDENT)
        )
        sessionManager.setAssociations(newList)

        // Entonces: lista actualizada y conjunto activo reseteado
        assertEquals(newList, sessionManager.session.value?.conjuntosAsociados)
        assertNull(sessionManager.session.value?.conjuntoActivoId)
    }

    @Test
    fun setAssociations_preservesAuth0Role() {
        // Dado: usuario con rol ADMIN de Auth0
        val userSession = UserSession(
            token = "jwt_token",
            userId = "auth0|admin1",
            name = "Admin User",
            role = UserRole.ADMIN
        )
        sessionManager.saveSession(userSession)

        // Cuando: llegan las asociaciones del backend
        sessionManager.setAssociations(
            listOf(ConjuntoUserAssociation("c1", "Mi Conjunto", UserRole.RESIDENT))
        )

        // Entonces: el rol de Auth0 NO se toca (solo cambia con setActiveConjunto)
        assertEquals(UserRole.ADMIN, sessionManager.currentRole())
    }

    @Test
    fun setAssociations_withEmptyList_clearsConjuntosAndResetsActive() {
        val userSession = UserSession(
            token = "jwt_token",
            userId = "auth0|new1",
            name = "New User",
            role = UserRole.USER
        )
        sessionManager.saveSession(userSession)

        sessionManager.setAssociations(emptyList())

        assertEquals(emptyList<ConjuntoUserAssociation>(), sessionManager.session.value?.conjuntosAsociados)
        assertNull(sessionManager.session.value?.conjuntoActivoId)
    }

    @Test
    fun setActiveConjunto_updatesRoleToAssociationRole() {
        // Dado: sesión con dos membresías
        val conjuntos = listOf(
            ConjuntoUserAssociation("c-admin", "Conjunto Admin", UserRole.ADMIN),
            ConjuntoUserAssociation("c-resident", "Conjunto Res", UserRole.RESIDENT)
        )
        val userSession = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "User",
            role = UserRole.USER,
            conjuntosAsociados = conjuntos
        )
        sessionManager.saveSession(userSession)

        // Cuando: elige el conjunto donde es ADMIN
        sessionManager.setActiveConjunto("c-admin")

        // Entonces: el rol de sesión refleja el rol en ese conjunto
        assertEquals(UserRole.ADMIN, sessionManager.currentRole())
        assertEquals("c-admin", sessionManager.session.value?.conjuntoActivoId)
    }

    @Test
    fun setAssociations_doesNothingWhenNoActiveSession() {
        // Dado: sin sesión activa
        // Cuando: se intenta setear asociaciones
        sessionManager.setAssociations(
            listOf(ConjuntoUserAssociation("c1", "Test", UserRole.RESIDENT))
        )
        // Entonces: no explota y la sesión sigue null
        assertNull(sessionManager.session.value)
    }
}
