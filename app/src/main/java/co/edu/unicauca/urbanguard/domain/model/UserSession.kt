package co.edu.unicauca.urbanguard.domain.model

data class UserSession(
    val token: String,
    val userId: String,
    val name: String,
    val role: UserRole,
    val conjuntosAsociados: List<ConjuntoUserAssociation> = emptyList(),
    val conjuntoActivoId: String? = null,
    // Nombre del conjunto activo cacheado en disco. [conjuntosAsociados] solo vive en
    // memoria y llega por red, asi que tras un arranque en frio la lista esta vacia y
    // el nombre quedaria sin resolver. Este campo permite mostrar el conjunto real
    // de inmediato; la lista autoritativa lo sobreescribe en cuanto se recarga.
    val conjuntoActivoNombre: String? = null
)

/**
 * Roles de la plataforma UrbanGuard.
 *
 * [USER] NO es un rol de negocio en Auth0; es un **centinela de enrutamiento** que indica
 * que el usuario autenticado no tiene rol asignado aún (staff=false y claim rol=null).
 * En ese estado el NavGraph lo dirige a ConjuntoSelectionScreen.
 *
 * [SUPERADMIN] se asigna cuando el claim https://urbanguard.com/staff = true (app_metadata.platform="superadmin").
 */
enum class UserRole { USER, RESIDENT, GUARD, ADMIN, SUPERADMIN }
