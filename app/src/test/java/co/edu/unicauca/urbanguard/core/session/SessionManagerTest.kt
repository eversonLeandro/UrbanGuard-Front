package co.edu.unicauca.urbanguard.core.session

import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        sessionManager = SessionManager(NoOpRolePersistence)
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
    fun setAssociations_clearsActiveConjuntoWhenItIsNoLongerInTheList() {
        // Dado: sesión activa con un conjunto activo previo
        val userSession = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "Test User",
            role = UserRole.USER,
            conjuntosAsociados = listOf(
                ConjuntoUserAssociation("old-1", "Antiguo Conjunto", UserRole.RESIDENT)
            ),
            conjuntoActivoId = "old-1",
            conjuntoActivoNombre = "Antiguo Conjunto"
        )
        sessionManager.saveSession(userSession)

        // Cuando: llega la lista real del backend, sin ese conjunto
        val newList = listOf(
            ConjuntoUserAssociation("real-1", "Torres del Prado", UserRole.ADMIN),
            ConjuntoUserAssociation("real-2", "Conjunto El Pinar", UserRole.RESIDENT)
        )
        sessionManager.setAssociations(newList)

        // Entonces: lista actualizada y conjunto activo (id y nombre) liberado
        assertEquals(newList, sessionManager.session.value?.conjuntosAsociados)
        assertNull(sessionManager.session.value?.conjuntoActivoId)
        assertNull(sessionManager.session.value?.conjuntoActivoNombre)
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

    @Test
    fun setAssociations_preservesActiveConjuntoWhenItIsStillInTheList() {
        // Dado: sesión restaurada con el conjunto activo (id y nombre desde disco)
        val restored = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "Test User",
            role = UserRole.RESIDENT,
            conjuntoActivoId = "c1",
            conjuntoActivoNombre = "Nombre Cacheado"
        )
        sessionManager.saveSession(restored)

        // Cuando: llega la lista real, que incluye ese conjunto
        val newList = listOf(
            ConjuntoUserAssociation("c1", "Torres del Prado", UserRole.RESIDENT),
            ConjuntoUserAssociation("c2", "El Pinar", UserRole.ADMIN)
        )
        sessionManager.setAssociations(newList)

        // Entonces: se conserva el conjunto activo y el nombre se refresca con el dato real
        assertEquals(newList, sessionManager.session.value?.conjuntosAsociados)
        assertEquals("c1", sessionManager.session.value?.conjuntoActivoId)
        assertEquals("Torres del Prado", sessionManager.session.value?.conjuntoActivoNombre)
        // Y el rol sigue sin tocarse: recargar la lista no es cambiar de conjunto
        assertEquals(UserRole.RESIDENT, sessionManager.currentRole())
    }

    @Test
    fun getActiveConjunto_resolvesNameFromPersistenceWhenListNotLoadedYet() {
        // Regresión del bug reportado: tras un arranque en frío la lista de conjuntos está
        // vacía (solo vive en memoria y llega por red), pero el id y el nombre sí se
        // restauran del disco. Antes el nombre se perdía y la UI pintaba un conjunto falso.
        val restored = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "Admin User",
            role = UserRole.ADMIN,
            conjuntosAsociados = emptyList(),
            conjuntoActivoId = "c1",
            conjuntoActivoNombre = "Torres del Prado"
        )
        sessionManager.saveSession(restored)

        val active = sessionManager.getActiveConjunto()

        assertEquals("c1", active?.conjuntoId)
        assertEquals("Torres del Prado", active?.nombre)
        assertEquals(UserRole.ADMIN, active?.roleEnConjunto)
    }

    @Test
    fun getActiveConjunto_returnsNullWhenThereIsNoNameToResolve() {
        // Sin lista y sin nombre persistido no se inventa nada: la UI muestra su estado vacío.
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|user1",
                name = "User",
                role = UserRole.USER
            )
        )

        assertNull(sessionManager.getActiveConjunto())
    }

    @Test
    fun getActiveConjunto_prefersTheAssociationsListOverThePersistedName() {
        val session = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "User",
            role = UserRole.RESIDENT,
            conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "El Pinar", UserRole.ADMIN)),
            conjuntoActivoId = "c1",
            conjuntoActivoNombre = "Nombre Antiguo"
        )
        sessionManager.saveSession(session)

        assertEquals("El Pinar", sessionManager.getActiveConjunto()?.nombre)
    }

    @Test
    fun setActiveConjunto_storesTheNameAndRoleOfTheAssociation() {
        val conjuntos = listOf(
            ConjuntoUserAssociation("c-admin", "Conjunto Admin", UserRole.ADMIN),
            ConjuntoUserAssociation("c-resident", "Conjunto Res", UserRole.RESIDENT)
        )
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|user1",
                name = "User",
                role = UserRole.USER,
                conjuntosAsociados = conjuntos
            )
        )

        sessionManager.setActiveConjunto("c-resident")

        assertEquals("c-resident", sessionManager.session.value?.conjuntoActivoId)
        assertEquals("Conjunto Res", sessionManager.session.value?.conjuntoActivoNombre)
        assertEquals(UserRole.RESIDENT, sessionManager.currentRole())
    }

    @Test
    fun setActiveConjunto_ignoresAConjuntoOutsideTheAssociations() {
        val session = UserSession(
            token = "jwt_token",
            userId = "auth0|user1",
            name = "User",
            role = UserRole.RESIDENT,
            conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "El Pinar", UserRole.RESIDENT)),
            conjuntoActivoId = "c1",
            conjuntoActivoNombre = "El Pinar"
        )
        sessionManager.saveSession(session)

        sessionManager.setActiveConjunto("ajeno")

        // La sesión no queda apuntando a un conjunto que el usuario no tiene
        assertEquals("c1", sessionManager.session.value?.conjuntoActivoId)
        assertEquals("El Pinar", sessionManager.session.value?.conjuntoActivoNombre)
        assertEquals(UserRole.RESIDENT, sessionManager.currentRole())
    }

    @Test
    fun activeConjuntoName_isPersistedBecauseItIsTheOnlyOfflineSource() {
        // El nombre es lo unico que permite mostrar el conjunto sin red, asi que tiene
        // que llegar a la copia cifrada; si no, el bug reaparece en cada arranque.
        val persistence = RecordingRolePersistence()
        val manager = SessionManager(persistence)
        manager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|user1",
                name = "Admin",
                role = UserRole.ADMIN,
                conjuntosAsociados = listOf(
                    ConjuntoUserAssociation("c1", "Torres del Prado", UserRole.ADMIN)
                )
            )
        )

        manager.setActiveConjunto("c1")

        assertEquals("c1", persistence.saved?.conjuntoActivoId)
        assertEquals("Torres del Prado", persistence.saved?.conjuntoActivoNombre)

        manager.clearSession()

        assertNull(persistence.saved)
        assertTrue(persistence.cleared)
    }

    private class RecordingRolePersistence : RolePersistence {
        var saved: PersistedRole? = null
        var cleared = false

        override fun save(persisted: PersistedRole) {
            saved = persisted
        }

        override fun read(): PersistedRole? = saved

        override fun clear() {
            saved = null
            cleared = true
        }
    }
}
