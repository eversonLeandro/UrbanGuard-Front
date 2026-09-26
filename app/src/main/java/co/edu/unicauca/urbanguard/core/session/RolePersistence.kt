package co.edu.unicauca.urbanguard.core.session

import co.edu.unicauca.urbanguard.domain.model.UserRole

/**
 * Identidad minima que hay que conservar entre reinicios del proceso para que la
 * app no pierda el rol del usuario.
 *
 * El claim de Auth0 solo transporta el rol de plataforma (ver
 * `Auth0Manager.mapCredentialsToSession`). El rol dentro de un conjunto
 * ([UserRole.ADMIN]) se deriva localmente al elegir conjunto, por lo que nunca
 * viaja en el token. Como [SessionManager] vive solo en memoria, al recrear la
 * Activity (cambio de tema claro/oscuro, rotacion o cierre del proceso) ese rol
 * se perdia y el NavGraph caia a la barra del residente.
 */
data class PersistedRole(
    val userId: String,
    val role: UserRole,
    val conjuntoActivoId: String?,
    // Se persiste tambien el nombre porque la lista de conjuntos asociados solo existe
    // en memoria y se recarga por red: sin el, un arranque en frio no podria resolver
    // que conjunto mostrar y la UI terminaba inventando un nombre.
    val conjuntoActivoNombre: String? = null
)

/**
 * Almacen cifrado del rol de sesion. La implementacion de produccion escribe en
 * almacenamiento seguro respaldado por el Android Keystore.
 */
interface RolePersistence {
    fun save(persisted: PersistedRole)
    fun read(): PersistedRole?
    fun clear()
}

/**
 * Implementacion sin persistencia, usada por defecto para que las pruebas
 * unitarias (JVM puro, sin Android Keystore) puedan construir [SessionManager]
 * sin dependencias.
 */
object NoOpRolePersistence : RolePersistence {
    override fun save(persisted: PersistedRole) = Unit
    override fun read(): PersistedRole? = null
    override fun clear() = Unit
}
